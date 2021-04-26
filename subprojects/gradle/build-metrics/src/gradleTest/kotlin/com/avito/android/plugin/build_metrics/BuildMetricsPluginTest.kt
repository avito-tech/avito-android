package com.avito.android.plugin.build_metrics

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val rootAppName = "root"

internal class BuildMetricsPluginTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.projectDir = tempDir
        TestProjectGenerator(
            name = rootAppName,
            plugins = plugins {
                id("com.avito.android.build-metrics")
            },
            modules = listOf(
                AndroidAppModule(name = "app")
            )
        ).generateIn(tempDir)
    }

    @Test
    fun `send nothing - dry run build`() {
        val result = build(":app:preBuild", dryRun = true)

        result.assertThat().buildSuccessful()

        val metrics = result.statsdMetrics()
        assertThat(metrics).isEmpty()
    }

    @Test
    fun `send configuration time - build`() {
        val result = build(":app:preBuild")

        result.assertThat()
            .buildSuccessful()

        result.expectMetric("time", "init_configuration.total")
    }

    @Test
    fun `send total build time - build`() {
        val result = build(":app:preBuild")

        result.assertThat()
            .buildSuccessful()

        result.expectMetric("time", "build-time.total")
    }

    @TestFactory
    fun `send app build time - build app`(): List<DynamicTest> {

        class Case(
            val description: String,
            val tasks: Array<String>,
            val metricName: String
        )
        return listOf(
            Case(
                description = "build app",
                tasks = arrayOf(":app:assembleDebug"),
                metricName = "app-build.app.packageDebug.finish"
            ),
            Case(
                description = "run instrumentation tests",
                tasks = arrayOf(":app:assembleDebug", ":app:assembleDebugAndroidTest"),
                metricName = "app-build.app.packageDebugAndroidTest.finish"
            ),
        ).map {
            dynamicTest("send total task time for app scenario - " + it.description) {
                val result = build(*it.tasks)

                result.assertThat()
                    .buildSuccessful()

                result.expectMetric("time", it.metricName)
            }
        }
    }

    private fun build(vararg tasks: String, dryRun: Boolean = false): TestResult {
        return gradlew(
            projectDir,
            *tasks,
            "-Pavito.build.metrics.enabled=true",
            "-Pavito.stats.enabled=false",
            "--debug", // to read statsd logs from stdout
            dryRun = dryRun
        )
    }
}
