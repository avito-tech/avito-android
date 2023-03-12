package com.avito.android.plugin.build_metrics.tasks

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.core.StubBuildMetricsSender
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.slow.SlowTasksMetricsTracker
import com.avito.graphite.series.SeriesName
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Task
import org.gradle.util.Path
import org.junit.jupiter.api.Test

internal class SlowTasksMetricsTrackerTest {

    @Test
    fun `sends - cumulative time of all tasks`() {
        val metrics = processResults(
            taskExecution(
                path = ":app:a",
                type = CustomTaskA::class.java,
                startMs = 0,
                endMs = 1_000
            ),
            taskExecution(
                path = ":lib:b",
                type = CustomTaskB::class.java,
                startMs = 0,
                endMs = 2_000
            ),
        )
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName.create("gradle.tasks", multipart = true),
                "3000"
            )
        )
    }

    @Test
    fun `sends - slow task types`() {
        val metrics = processResults(
            taskExecution(
                path = ":app:a1",
                type = CustomTaskA::class.java,
                startMs = 0,
                endMs = 10_000
            ),
            taskExecution(
                path = ":app:a2",
                type = CustomTaskA::class.java,
                startMs = 0,
                endMs = 20_000
            ),
            taskExecution(
                path = ":app:b",
                type = CustomTaskB::class.java,
                startMs = 0,
                endMs = 20_000
            ),
        )
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName.create("gradle.slow.task.type", multipart = true)
                    .addTag("task_type", "CustomTaskA"),
                "30000"
            )
        )
    }

    @Test
    fun `sends - slow modules`() {
        val metrics = processResults(
            taskExecution(
                path = ":app:a",
                type = CustomTaskA::class.java,
                startMs = 0,
                endMs = 11_000
            ),
            taskExecution(
                path = ":app:b",
                type = CustomTaskB::class.java,
                startMs = 0,
                endMs = 12_000
            ),
            taskExecution(
                path = ":lib:a",
                type = CustomTaskA::class.java,
                startMs = 0,
                endMs = 12_000
            ),
        )
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName.create("gradle.slow.module", multipart = true)
                    .addTag("module_name", "app"),
                "23000"
            )
        )
    }

    @Test
    fun `sends - slow tasks types with modules`() {
        val metrics = processResults(
            taskExecution(
                path = ":app:a1",
                type = CustomTaskA::class.java,
                startMs = 0,
                endMs = 10_000
            ),
            taskExecution(
                path = ":app:a2",
                type = CustomTaskA::class.java,
                startMs = 0,
                endMs = 10_000
            ),
            taskExecution(
                path = ":lib:b",
                type = CustomTaskB::class.java,
                startMs = 0,
                endMs = 20_000
            )
        )
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName.create("gradle.slow.module.task.type", multipart = true)
                    .addTag("module_name", "app")
                    .addTag("task_type", "CustomTaskA"),
                "20000"
            )
        )
    }

    private fun taskExecution(
        path: String,
        type: Class<out Task>,
        startMs: Long = 0,
        endMs: Long = 1,
        cacheResult: TaskCacheResult = TaskCacheResult.Disabled
    ) = TaskExecutionResult(
        path = Path.path(path),
        type = type,
        startMs = startMs,
        endMs = endMs,
        cacheResult = cacheResult
    )

    private fun processResults(vararg tasks: TaskExecutionResult): List<GraphiteMetric> {
        val buildMetricSender = StubBuildMetricsSender()
        val listener = SlowTasksMetricsTracker(buildMetricSender)
        val result = BuildOperationsResult(
            tasksExecutions = tasks.toList(),
            cacheOperations = CacheOperations(
                errors = emptyList()
            )
        )
        listener.onBuildFinished(result)

        return buildMetricSender.getSentGraphiteMetrics()
    }

    private abstract class CustomTaskA : Task
    private abstract class CustomTaskB : Task
}
