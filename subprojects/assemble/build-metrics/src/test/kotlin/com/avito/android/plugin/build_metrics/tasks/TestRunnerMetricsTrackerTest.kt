package com.avito.android.plugin.build_metrics.tasks

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.core.StubBuildMetricsSender
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.testrunner.TestRunnerMetricsTracker
import com.avito.android.plugin.build_metrics.internal.result.BuildResult
import com.avito.android.plugin.build_metrics.internal.result.BuildStatus
import com.avito.graphite.series.SeriesName
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Task
import org.gradle.util.Path
import org.junit.jupiter.api.Test
import java.time.Instant

internal class TestRunnerMetricsTrackerTest {

    @Test
    fun `sends instrumentation test runner execution time per module`() {
        val metricsTags = mapOf(
            "runner_type" to "instrumentation",
            "runner_version" to "2025.1",
        )

        val metrics = processResults(
            taskExecution(
                path = ":app:instrumentationTest",
                type = InstrumentationTestsTask::class.java,
                tags = metricsTags,
                startMs = 0,
                endMs = 30_000
            ),
            taskExecution(
                path = ":lib:instrumentationTest",
                type = InstrumentationTestsTask::class.java,
                tags = metricsTags,
                startMs = 0,
                endMs = 20_000
            ),
        )

        assertThat(metrics).containsExactly(
            GraphiteMetric(
                SeriesName.create("gradle.tests.execution_time.module", multipart = true)
                    .addTag("module_name", "app")
                    .addTag("runner_type", "instrumentation")
                    .addTag("runner_version", "2025.1"),
                "30000"
            ),
            GraphiteMetric(
                SeriesName.create("gradle.tests.execution_time.module", multipart = true)
                    .addTag("module_name", "lib")
                    .addTag("runner_type", "instrumentation")
                    .addTag("runner_version", "2025.1"),
                "20000"
            )
        )
    }

    @Test
    fun `sends emcee test runner execution time per module`() {
        val metricsTags = mapOf(
            "runner_type" to "emcee",
            "runner_version" to "1.2.0",
        )

        val metrics = processResults(
            taskExecution(
                path = ":app:emceeTest",
                type = AvitoEmceeTestTask::class.java,
                tags = metricsTags,
                startMs = 0,
                endMs = 15_000
            ),
            taskExecution(
                path = ":lib:emceeTest",
                type = AvitoEmceeTestTask::class.java,
                tags = metricsTags,
                startMs = 0,
                endMs = 10_000
            ),
        )

        assertThat(metrics).containsExactly(
            GraphiteMetric(
                SeriesName.create("gradle.tests.execution_time.module", multipart = true)
                    .addTag("module_name", "app")
                    .addTag("runner_type", "emcee")
                    .addTag("runner_version", "1.2.0"),
                "15000"
            ),
            GraphiteMetric(
                SeriesName.create("gradle.tests.execution_time.module", multipart = true)
                    .addTag("module_name", "lib")
                    .addTag("runner_type", "emcee")
                    .addTag("runner_version", "1.2.0"),
                "10000"
            )
        )
    }

    @Test
    fun `does not send metric when no tasks with test runner metrics metadata found`() {
        val metrics = processResults(
            taskExecution(
                path = ":app:compile",
                type = CustomTask::class.java,
                tags = emptyMap(),
                startMs = 0,
                endMs = 30_000
            ),
        )

        assertThat(metrics).isEmpty()
    }

    @Test
    fun `does not send metric when total time is zero`() {
        val metricsTags = mapOf(
            "runner_type" to "instrumentation",
            "runner_version" to "2025.1",
        )

        val metrics = processResults(
            taskExecution(
                path = ":app:instrumentationTest",
                type = InstrumentationTestsTask::class.java,
                tags = metricsTags,
                startMs = 0,
                endMs = 0
            ),
        )

        assertThat(metrics).isEmpty()
    }

    @Test
    fun `processes multiple runner types independently`() {
        val instrumentationTags = mapOf(
            "runner_type" to "instrumentation",
            "runner_version" to "2025.1",
        )

        val emceeTags = mapOf(
            "runner_type" to "emcee",
            "runner_version" to "1.2.0",
        )

        val metrics = processResults(
            taskExecution(
                path = ":app:instrumentationTest",
                type = InstrumentationTestsTask::class.java,
                tags = instrumentationTags,
                startMs = 0,
                endMs = 30_000
            ),
            taskExecution(
                path = ":app:emceeTest",
                type = AvitoEmceeTestTask::class.java,
                tags = emceeTags,
                startMs = 0,
                endMs = 20_000
            ),
        )

        assertThat(metrics).containsExactly(
            GraphiteMetric(
                SeriesName.create("gradle.tests.execution_time.module", multipart = true)
                    .addTag("module_name", "app")
                    .addTag("runner_type", "instrumentation")
                    .addTag("runner_version", "2025.1"),
                "30000"
            ),
            GraphiteMetric(
                SeriesName.create("gradle.tests.execution_time.module", multipart = true)
                    .addTag("module_name", "app")
                    .addTag("runner_type", "emcee")
                    .addTag("runner_version", "1.2.0"),
                "20000"
            )
        )
    }

    @Test
    fun `sums multiple tasks in same module with same runner type and version`() {
        val metricsTags = mapOf(
            "runner_type" to "instrumentation",
            "runner_version" to "2025.1",
        )

        val metrics = processResults(
            taskExecution(
                path = ":app:instrumentationTestApi27",
                type = InstrumentationTestsTask::class.java,
                tags = metricsTags,
                startMs = 0,
                endMs = 15_000
            ),
            taskExecution(
                path = ":app:instrumentationTestApi35",
                type = InstrumentationTestsTask::class.java,
                tags = metricsTags,
                startMs = 0,
                endMs = 10_000
            ),
        )

        assertThat(metrics).containsExactly(
            GraphiteMetric(
                SeriesName.create("gradle.tests.execution_time.module", multipart = true)
                    .addTag("module_name", "app")
                    .addTag("runner_type", "instrumentation")
                    .addTag("runner_version", "2025.1"),
                "25000" // Sum of both tasks
            )
        )
    }

    @Test
    fun `handles different runner versions in same module separately`() {
        val instrumentationTags = mapOf(
            "runner_type" to "instrumentation",
            "runner_version" to "2025.1",
        )

        val emceeTags = mapOf(
            "runner_type" to "emcee",
            "runner_version" to "1.2.0",
        )

        val metrics = processResults(
            taskExecution(
                path = ":app:instrumentationTest",
                type = InstrumentationTestsTask::class.java,
                tags = instrumentationTags,
                startMs = 0,
                endMs = 15_000
            ),
            taskExecution(
                path = ":app:emceeTest",
                type = AvitoEmceeTestTask::class.java,
                tags = emceeTags,
                startMs = 0,
                endMs = 10_000
            ),
        )

        assertThat(metrics).containsExactly(
            GraphiteMetric(
                SeriesName.create("gradle.tests.execution_time.module", multipart = true)
                    .addTag("module_name", "app")
                    .addTag("runner_type", "instrumentation")
                    .addTag("runner_version", "2025.1"),
                "15000"
            ),
            GraphiteMetric(
                SeriesName.create("gradle.tests.execution_time.module", multipart = true)
                    .addTag("module_name", "app")
                    .addTag("runner_type", "emcee")
                    .addTag("runner_version", "1.2.0"),
                "10000"
            )
        )
    }

    private fun taskExecution(
        name: String = "stub",
        path: String,
        type: Class<out Task>,
        tags: Map<String, String> = emptyMap(),
        startMs: Long = 0,
        endMs: Long = 1,
        cacheResult: TaskCacheResult = TaskCacheResult.Disabled
    ) = TaskExecutionResult(
        name = name,
        path = Path.path(path),
        type = type,
        startMs = startMs,
        endMs = endMs,
        cacheResult = cacheResult,
        tags = tags,
    )

    private fun processResults(
        vararg tasks: TaskExecutionResult
    ): List<GraphiteMetric> {
        val buildMetricSender = StubBuildMetricsSender()
        val listener = TestRunnerMetricsTracker(
            metricsTracker = buildMetricSender,
        )
        val result = BuildOperationsResult(
            tasksExecutions = tasks.toList(),
            cacheOperations = CacheOperations(
                errors = emptyList()
            ),
            buildResult = BuildResult(
                status = BuildStatus.Success,
                startTime = Instant.now(),
                configurationEndTime = Instant.now(),
                finishTime = Instant.now(),
            )
        )
        listener.onBuildFinished(result)

        return buildMetricSender.getSentGraphiteMetrics()
    }

    private abstract class InstrumentationTestsTask : Task
    private abstract class CustomTask : Task
    private abstract class AvitoEmceeTestTask : Task
}
