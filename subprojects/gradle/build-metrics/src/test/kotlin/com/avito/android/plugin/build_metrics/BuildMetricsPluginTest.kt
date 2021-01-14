package com.avito.android.plugin.build_metrics

import com.avito.git.Git
import com.avito.logger.StubLoggerFactory
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val rootAppName = "root"
private const val loggerPrefix = "[StatsDSender@:] "

class BuildMetricsPluginTest {

    private lateinit var tempDir: File
    private val loggerFactory = StubLoggerFactory
    private val git: Git by lazy {
        Git.Impl(
            rootDir = tempDir,
            loggerFactory = loggerFactory
        )
    }

    private val syncBranch = "develop"

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.tempDir = tempDir
        TestProjectGenerator(
            name = rootAppName,
            plugins = listOf("com.avito.android.build-metrics"),
            modules = listOf(
                AndroidAppModule(name = "app")
            )
        ).generateIn(tempDir)

        with(git) {
            init()
            checkout(branchName = syncBranch, create = true)
            addAll()
            commit("initial commit")
        }
    }

    @Test
    fun `send nothing - dry run build`() {
        val result = build(":app:preBuild", dryRun = true)

        result.assertThat().buildSuccessful()

        result.expectNoMetrics()
    }

    @TestFactory
    fun `send nothing - the most common tasks without real work`(): List<DynamicTest> {
        return listOf(
            "help",
            "tasks",
            ":app:tasks",
            "cleanBuildCache",
            ":app:cleanBuildCache",
            "clean",
            ":app:clean",
            ":app:dependencies"
        ).map {
            val task = it
            dynamicTest("send nothing - no real work by $task") {
                val result = build(task)

                result.assertThat().buildSuccessful()

                result.expectNoMetrics()
            }
        }
    }

    @Test
    fun `send configuration time - build`() {
        val result = build(":app:preBuild")

        result.assertThat()
            .buildSuccessful()

        result.expectMetric("time", "init_configuration.total")
    }

    @Test
    fun `send total build time`() {
        val result = build(":app:preBuild")

        result.assertThat()
            .buildSuccessful()

        result.expectMetric("time", "build-time.total")
    }

    @TestFactory
    fun `send build tasks`(): List<DynamicTest> {

        class Case(
            val description: String,
            val tasks: Array<String>,
            val metricName: String
        )
        return listOf(
            Case(
                description = "build app",
                tasks = arrayOf(":app:assemble"),
                metricName = "build-tasks.app_assemble"
            ),
            Case(
                description = "run instrumentation tests",
                tasks = arrayOf(":app:assembleDebug", ":app:assembleDebugAndroidTest"),
                metricName = "build-tasks.app_assembleDebug_app_assembleDebugAndroidTest"
            ),
            Case(
                description = "stable tasks order",
                tasks = arrayOf(":app:assembleDebugAndroidTest", ":app:assembleDebug"),
                metricName = "build-tasks.app_assembleDebug_app_assembleDebugAndroidTest"
            ),
            Case(
                description = "up to three tasks",
                tasks = arrayOf(":app:preBuild", ":app:assembleDebug", ":app:assembleDebugAndroidTest"),
                metricName = "build-tasks._"
            )
        ).map {
            dynamicTest("send build tasks - " + it.description) {
                val result = build(*it.tasks)

                result.assertThat()
                    .buildSuccessful()

                result.expectMetric("time", it.metricName)
            }
        }
    }

    private fun TestResult.expectNoMetrics() {
        assertWithMessage("").that(output).doesNotContain(loggerPrefix)
    }

    private fun TestResult.expectMetric(type: String, metricName: String) {
        // example: time:apps.mobile.statistic.android.local.user.id.success.init_configuration.total:5821
        val metrics = output.lines()
            .filter { it.contains(loggerPrefix) }
            .map { it.substringAfter(loggerPrefix) }
            .map { it.substringBeforeLast(':') }

        val target = metrics
            .filter { it.startsWith(type) }
            .filter { it.endsWith(".$metricName") }

        assertWithMessage("Expected metric $type $metricName in $metrics")
            .that(target).hasSize(1)
    }

    private fun build(vararg tasks: String, dryRun: Boolean = false): TestResult {
        return gradlew(
            tempDir, *tasks,
            "-Pavito.build.metrics.enabled=true",
            "-Pavito.stats.enabled=true",
            "-Pavito.stats.host=http://stats",
            "-Pavito.stats.fallbackHost=http://stats",
            "-Pavito.stats.port=80",
            "-Pavito.stats.namespace=android",
            "--debug", // to read sentry logs from stdout
            dryRun = dryRun
        )
    }
}
