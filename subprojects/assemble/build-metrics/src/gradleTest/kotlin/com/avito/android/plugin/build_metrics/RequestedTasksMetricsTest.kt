package com.avito.android.plugin.build_metrics

import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.BuildExecutionHistory
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class RequestedTasksMetricsTest {

    private lateinit var projectDir: File
    private lateinit var repoNameForHistory: String

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.projectDir = tempDir
        this.repoNameForHistory = "test/${tempDir.name}"
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.build-metrics")
                id("com.avito.android.gradle-logger")
            },
            imports = listOf(
                "import com.avito.android.plugin.build_metrics.BuildEnvironment",
            ),
            buildGradleExtra = """
                |buildMetrics {
                |   buildType.set("test")
                |   environment.set(BuildEnvironment.CI)
                |   sendRequestedTasksMetrics.set(true)
                |   repoName.set("$repoNameForHistory")
                |}
            """.trimMargin(),
            modules = listOf(
                AndroidAppModule(name = "app")
            ),
        ).generateIn(tempDir)
    }

    @Test
    fun `sends requested task duration metric - single task`() {
        val result = build(":app:preBuild")

        result.assertThat().buildSuccessful()

        assertHasRequestedTaskMetric(result, "task_name=app_preBuild")
    }

    @Test
    fun `sends requested task duration metric - multiple tasks`() {
        val result = build(":app:preBuild", ":app:preDebugBuild")

        result.assertThat().buildSuccessful()

        assertHasRequestedTaskMetric(result, "task_name=app_preBuild")
        assertHasRequestedTaskMetric(result, "task_name=app_preDebugBuild")
    }

    @Test
    fun `does not send metric - sendRequestedTasksMetrics is false`() {
        val dir = projectDir
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.build-metrics")
                id("com.avito.android.gradle-logger")
            },
            imports = listOf(
                "import com.avito.android.plugin.build_metrics.BuildEnvironment",
            ),
            buildGradleExtra = """
                |buildMetrics {
                |   buildType.set("test")
                |   environment.set(BuildEnvironment.CI)
                |   sendRequestedTasksMetrics.set(false)
                |}
            """.trimMargin(),
            modules = listOf(
                AndroidAppModule(name = "app")
            ),
        ).generateIn(dir)

        val result = BuildMetricsRunner(dir, configurationCache = true)
            .build(listOf(":app:preBuild"))

        result.assertThat().buildSuccessful()

        val metrics = result.graphiteMetrics()
        val requestedTaskMetrics = metrics.filter { it.contains("gradle.requested_task.duration") }
        assertWithMessage("Expected no requested task metrics but found: $requestedTaskMetrics")
            .that(requestedTaskMetrics).isEmpty()
    }

    private fun assertHasRequestedTaskMetric(
        result: com.avito.test.gradle.TestResult,
        tagSubstring: String,
    ) {
        val metrics = result.graphiteMetrics()
        val matched = metrics.filter {
            it.contains("gradle.requested_task.duration") && it.contains(tagSubstring)
        }
        assertWithMessage(
            "Expected metric with tag ($tagSubstring) in gradle.requested_task.duration. " +
                "Metrics: $metrics"
        ).that(matched).isNotEmpty()
    }

    @Test
    fun `creates execution history file after first build`() {
        build(":app:preBuild")

        val historyFile = executionHistoryFile()
        assertWithMessage("Execution history file should be created after first build")
            .that(historyFile.exists()).isTrue()

        val properties = java.util.Properties()
        historyFile.inputStream().use { properties.load(it) }
        assertWithMessage("Execution history should contain commit key")
            .that(properties.containsKey("commit")).isTrue()
        assertWithMessage("Commit should not be empty")
            .that(properties.getProperty("commit")).isNotEmpty()
    }

    @Test
    fun `first build includes commit_changed=false`() {
        val result = build(":app:preBuild")

        result.assertThat().buildSuccessful()

        val metrics = result.graphiteMetrics()
        val requestedTaskMetrics = metrics.filter { it.contains("gradle.requested_task.duration") }
        assertWithMessage("Should have requested task metrics")
            .that(requestedTaskMetrics).isNotEmpty()

        requestedTaskMetrics.forEach { metric ->
            assertWithMessage("First build should have commit_changed=false: $metric")
                .that(metric).contains("commit_changed=false")
        }
    }

    @Test
    fun `second build includes commit_changed=false - same branch`() {
        val firstResult = build(":app:preBuild")
        firstResult.assertThat().buildSuccessful()

        val secondResult = BuildMetricsRunner(projectDir, configurationCache = false)
            .build(listOf(":app:preBuild"))
        secondResult.assertThat().buildSuccessful()

        val metrics = secondResult.graphiteMetrics()
        val requestedTaskMetrics = metrics.filter { it.contains("gradle.requested_task.duration") }
        assertWithMessage("Should have requested task metrics on second build")
            .that(requestedTaskMetrics).isNotEmpty()

        requestedTaskMetrics.forEach { metric ->
            assertWithMessage("Second build on same commit should have commit_changed=false: $metric")
                .that(metric).contains("commit_changed=false")
        }
    }

    @Test
    fun `second build detects commit change after manual history file edit`() {
        val firstResult = build(":app:preBuild")
        firstResult.assertThat().buildSuccessful()

        val historyFile = executionHistoryFile()
        assertWithMessage("History file should exist after first build")
            .that(historyFile.exists()).isTrue()

        val properties = java.util.Properties()
        properties.setProperty("commit", "old-commit-hash")
        historyFile.outputStream().use { properties.store(it, null) }

        val secondResult = BuildMetricsRunner(projectDir, configurationCache = false)
            .build(listOf(":app:preBuild"))
        secondResult.assertThat().buildSuccessful()

        val metrics = secondResult.graphiteMetrics()
        val requestedTaskMetrics = metrics.filter { it.contains("gradle.requested_task.duration") }

        requestedTaskMetrics.forEach { metric ->
            assertWithMessage("Should detect commit change: $metric")
                .that(metric).contains("commit_changed=true")
        }
    }

    private fun build(vararg args: String) =
        BuildMetricsRunner(projectDir, configurationCache = true)
            .build(args.toList())

    private fun executionHistoryFile(): File {
        val key = BuildExecutionHistory.storageKey(
            originUrl = null,
            repoName = repoNameForHistory,
        )
        val testKitDir = File(System.getProperty("buildDir"), "test-kit-dir")
        return File(testKitDir, "build-metrics/execution-history/$key.properties")
    }
}
