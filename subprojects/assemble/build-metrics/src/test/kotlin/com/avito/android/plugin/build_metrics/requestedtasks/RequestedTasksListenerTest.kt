package com.avito.android.plugin.build_metrics.requestedtasks

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.core.StubBuildMetricsSender
import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.BuildExecutionHistory
import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.BuildExecutionState
import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.RequestedTasksListener
import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.RequestedTasksMetadata
import com.avito.android.plugin.build_metrics.internal.result.BuildResult
import com.avito.android.plugin.build_metrics.internal.result.BuildStatus
import com.avito.graphite.series.SeriesName
import com.avito.logger.PrintlnLoggerFactory
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Instant

internal class RequestedTasksListenerTest {

    private val defaultBootstrapTaskNames: Set<String> = setOf(
        "installGitHooks",
        "checkBuildEnvironment",
        "checkModulesOwners",
        "checkAnvilConfiguration",
        "checkModuleTypeNotDeprecated",
    )

    @Test
    fun `sends graphite duration metric - single requested task`(@TempDir tempDir: File) {
        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(":app:assembleDebug"),
            buildDurationMs = 5000L,
        )

        val expectedSeries = baseSeries()
            .addTag("task_name", "app_assembleDebug")

        assertThat(result.graphiteMetrics).contains(
            GraphiteMetric(expectedSeries, "5000")
        )
    }

    @Test
    fun `sends metrics for each requested task - multiple tasks`(@TempDir tempDir: File) {
        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(":app:assembleDebug", ":lib:assembleDebug"),
            buildDurationMs = 3000L,
        )

        assertThat(result.graphiteMetrics).hasSize(2)

        val taskNames = result.graphiteMetrics.map {
            it.path.asAspect().substringAfter("task_name=").substringBefore(";")
        }
        assertThat(taskNames).containsExactly("app_assembleDebug", "lib_assembleDebug")
    }

    @Test
    fun `reports only assembleDebug when present - higher priority than gradleSync`(@TempDir tempDir: File) {
        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(
                ":checkBuildEnvironment",
                ":app:assembleDebug",
            ),
            buildDurationMs = 2000L,
        )

        assertThat(result.graphiteMetrics).hasSize(1)
        val expectedSeries = baseSeries().addTag("task_name", "app_assembleDebug")
        assertThat(result.graphiteMetrics).contains(
            GraphiteMetric(expectedSeries, "2000")
        )
    }

    @Test
    fun `reports only assembleRelease when present - higher priority than gradleSync`(@TempDir tempDir: File) {
        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(
                ":checkBuildEnvironment",
                ":app:assembleRelease",
            ),
            buildDurationMs = 2000L,
        )

        assertThat(result.graphiteMetrics).hasSize(1)
        val expectedSeries = baseSeries().addTag("task_name", "app_assembleRelease")
        assertThat(result.graphiteMetrics).contains(
            GraphiteMetric(expectedSeries, "2000")
        )
    }

    @Test
    fun `reports only assembleDebug and assembleRelease - filters out other tasks`(@TempDir tempDir: File) {
        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(":app:assembleDebug", ":app:assembleRelease", ":checkBuildEnvironment"),
            buildDurationMs = 3000L,
        )

        assertThat(result.graphiteMetrics).hasSize(2)
        val taskNames = result.graphiteMetrics.map {
            it.path.asAspect().substringAfter("task_name=").substringBefore(";")
        }
        assertThat(taskNames).containsExactly("app_assembleDebug", "app_assembleRelease")
    }

    @Test
    fun `sends single gradleSync metric - when requested tasks list is empty`(
        @TempDir tempDir: File,
    ) {
        val result = processResults(
            tempDir = tempDir,
            requestedTasks = emptyList(),
            buildDurationMs = 2000L,
        )

        assertThat(result.graphiteMetrics).hasSize(1)
        val expectedSeries = baseSeries().addTag("task_name", "gradleSync")
        assertThat(result.graphiteMetrics).contains(
            GraphiteMetric(expectedSeries, "2000")
        )
    }

    @Test
    fun `sends single gradleSync metric - when requested tasks contain only bootstrap tasks`(
        @TempDir tempDir: File,
    ) {
        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(
                ":installGitHooks",
                ":checkBuildEnvironment",
            ),
            buildDurationMs = 2000L,
        )

        assertThat(result.graphiteMetrics).hasSize(1)
        val expectedSeries = baseSeries().addTag("task_name", "gradleSync")
        assertThat(result.graphiteMetrics).contains(
            GraphiteMetric(expectedSeries, "2000")
        )
    }

    @Test
    fun `filters out bootstrap tasks from requested task metrics`(@TempDir tempDir: File) {
        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(
                ":installGitHooks",
                ":app:preBuild",
            ),
            buildDurationMs = 1234L,
        )

        assertThat(result.graphiteMetrics).hasSize(1)
        val expectedSeries = baseSeries().addTag("task_name", "app_preBuild")
        assertThat(result.graphiteMetrics).contains(
            GraphiteMetric(expectedSeries, "1234")
        )
    }

    @Test
    fun `includes commit_changed=false - first build with no history`(@TempDir tempDir: File) {
        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(":app:assembleDebug"),
            buildDurationMs = 1000L,
        )

        val expectedSeries = baseSeries()
            .addTag("task_name", "app_assembleDebug")

        assertThat(result.graphiteMetrics).contains(
            GraphiteMetric(expectedSeries, "1000")
        )
    }

    @Test
    fun `includes commit_changed=false - same branch and commit`(@TempDir tempDir: File) {
        val historyFile = File(tempDir, "execution-history.properties")
        val history = BuildExecutionHistory(historyFile, PrintlnLoggerFactory)
        history.writeCurrentState(BuildExecutionState(commit = "abc123"))

        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(":app:assembleDebug"),
            commitHash = "abc123",
            buildDurationMs = 1000L,
        )

        val expectedSeries = baseSeries(commitChanged = "false")
            .addTag("task_name", "app_assembleDebug")

        assertThat(result.graphiteMetrics).contains(
            GraphiteMetric(expectedSeries, "1000")
        )
    }

    @Test
    fun `includes commit_changed=true - branch and commit changed since last build`(@TempDir tempDir: File) {
        val historyFile = File(tempDir, "execution-history.properties")
        val history = BuildExecutionHistory(historyFile, PrintlnLoggerFactory)
        history.writeCurrentState(BuildExecutionState(commit = "abc123"))

        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(":app:assembleDebug"),
            commitHash = "def456",
            buildDurationMs = 2000L,
        )

        val expectedSeries = baseSeries(commitChanged = "true")
            .addTag("task_name", "app_assembleDebug")

        assertThat(result.graphiteMetrics).contains(
            GraphiteMetric(expectedSeries, "2000")
        )
    }

    @Test
    fun `includes commit_changed=true - new commit on same branch`(@TempDir tempDir: File) {
        val historyFile = File(tempDir, "execution-history.properties")
        val history = BuildExecutionHistory(historyFile, PrintlnLoggerFactory)
        history.writeCurrentState(BuildExecutionState(commit = "abc123"))

        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(":app:assembleDebug"),
            commitHash = "def456",
            buildDurationMs = 1000L,
        )

        val expectedSeries = baseSeries(commitChanged = "true")
            .addTag("task_name", "app_assembleDebug")

        assertThat(result.graphiteMetrics).contains(
            GraphiteMetric(expectedSeries, "1000")
        )
    }

    @Test
    fun `sanitizes task name - removes colon prefix and replaces colons`() {
        assertThat(RequestedTasksListener.sanitizeTaskName(":app:assembleDebug"))
            .isEqualTo("app_assembleDebug")
        assertThat(RequestedTasksListener.sanitizeTaskName("assembleDebug"))
            .isEqualTo("assembleDebug")
        assertThat(RequestedTasksListener.sanitizeTaskName(":lib:sub:compileKotlin"))
            .isEqualTo("lib_sub_compileKotlin")
    }

    @Test
    fun `includes is_invoked_from_ide=true - when invoked from IDE`(@TempDir tempDir: File) {
        val result = processResults(
            tempDir = tempDir,
            requestedTasks = listOf(":app:assembleDebug"),
            isInvokedFromIde = true,
            buildDurationMs = 1000L,
        )

        val metricAspect = result.graphiteMetrics.first().path.asAspect()
        assertThat(metricAspect).contains("is_invoked_from_ide=true")
    }

    @Test
    fun `writes current state to history after sending metrics`(@TempDir tempDir: File) {
        val historyFile = File(tempDir, "execution-history.properties")

        processResults(
            tempDir = tempDir,
            requestedTasks = listOf(":app:assembleDebug"),
            commitHash = "abc123",
            buildDurationMs = 1000L,
        )

        val history = BuildExecutionHistory(historyFile, PrintlnLoggerFactory)
        val savedState = history.readPreviousState()
        assertThat(savedState).isNotNull()
        assertThat(savedState!!.commit).isEqualTo("abc123")
    }

    private data class MetricsResult(
        val graphiteMetrics: List<GraphiteMetric>,
    )

    private fun baseSeries(
        commitChanged: String = "false",
    ): SeriesName {
        return SeriesName.create("gradle.requested_task.duration", multipart = true)
            .addTag("user_name", "testuser")
            .addTag("is_invoked_from_ide", "false")
            .addTag("ram_gb", "16")
            .addTag("commit_changed", commitChanged)
    }

    private fun processResults(
        tempDir: File,
        requestedTasks: List<String>,
        userName: String = "testuser",
        commitHash: String = "abc123",
        isInvokedFromIde: Boolean = false,
        buildDurationMs: Long = 5000L,
    ): MetricsResult {
        val sender = StubBuildMetricsSender()
        val historyFile = File(tempDir, "execution-history.properties")

        val listener = RequestedTasksListener(
            sender = sender,
            requestedTasks = requestedTasks,
            bootstrapRequestedTaskNames = defaultBootstrapTaskNames,
            metadata = RequestedTasksMetadata(
                userName = userName,
                commitHashProvider = lazyOf(commitHash),
                isInvokedFromIde = isInvokedFromIde,
            ),
            executionHistory = BuildExecutionHistory(historyFile, PrintlnLoggerFactory),
            hardwareInfoProvider = StubHardwareInfoProvider(),
        )

        val result = BuildOperationsResult(
            tasksExecutions = emptyList(),
            cacheOperations = CacheOperations(errors = emptyList()),
            buildResult = BuildResult(
                status = BuildStatus.Success,
                startTime = Instant.ofEpochMilli(0),
                configurationEndTime = Instant.ofEpochMilli(1000),
                finishTime = Instant.ofEpochMilli(buildDurationMs),
            ),
        )

        listener.onBuildFinished(result)
        return MetricsResult(
            graphiteMetrics = sender.getSentGraphiteMetrics(),
        )
    }
}
