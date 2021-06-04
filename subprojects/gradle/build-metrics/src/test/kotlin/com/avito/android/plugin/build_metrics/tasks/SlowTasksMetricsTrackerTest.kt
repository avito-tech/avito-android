package com.avito.android.plugin.build_metrics.tasks

import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.tasks.SlowTasksMetricsTracker
import com.avito.android.sentry.StubEnvironmentInfo
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsMetric
import com.avito.android.stats.StubStatsdSender
import com.avito.android.stats.TimeMetric
import com.avito.utils.gradle.Environment
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Task
import org.gradle.util.Path
import org.junit.jupiter.api.Test

internal class SlowTasksMetricsTrackerTest {

    private val envInfo = StubEnvironmentInfo(
        node = "user",
        environment = Environment.Local
    )

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
            TimeMetric(
                SeriesName.create("local", "user", "build", "tasks", "cumulative", "any"),
                timeInMs = 3_000
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
                endMs = 1_000
            ),
            taskExecution(
                path = ":app:a2",
                type = CustomTaskA::class.java,
                startMs = 0,
                endMs = 2_000
            ),
            taskExecution(
                path = ":app:b",
                type = CustomTaskB::class.java,
                startMs = 0,
                endMs = 2_000
            ),
        )
        assertThat(metrics).contains(
            TimeMetric(
                SeriesName.create("local", "user", "build", "tasks", "slow", "type", "CustomTaskA"),
                timeInMs = 3_000
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
                endMs = 1_000
            ),
            taskExecution(
                path = ":app:b",
                type = CustomTaskB::class.java,
                startMs = 0,
                endMs = 2_000
            ),
            taskExecution(
                path = ":lib:a",
                type = CustomTaskA::class.java,
                startMs = 0,
                endMs = 2_000
            ),
        )
        assertThat(metrics).contains(
            TimeMetric(
                SeriesName.create("local", "user", "build", "tasks", "slow", "module", "app"),
                timeInMs = 3_000
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
                endMs = 1_000
            ),
            taskExecution(
                path = ":app:a2",
                type = CustomTaskA::class.java,
                startMs = 0,
                endMs = 1_000
            ),
            taskExecution(
                path = ":lib:b",
                type = CustomTaskB::class.java,
                startMs = 0,
                endMs = 2_000
            )
        )
        assertThat(metrics).contains(
            TimeMetric(
                SeriesName.create("local", "user", "build", "tasks", "slow", "task", "app", "CustomTaskA"),
                timeInMs = 2_000
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

    private fun processResults(vararg tasks: TaskExecutionResult): List<StatsMetric> {
        val statsd = StubStatsdSender()
        val metricsTracker = BuildMetricTracker(envInfo, statsd)

        val listener = SlowTasksMetricsTracker(metricsTracker)
        val result = BuildOperationsResult(
            tasksExecutions = tasks.toList(),
            cacheOperations = CacheOperations(
                errors = emptyList()
            )
        )
        listener.onBuildFinished(result)

        return statsd.getSentMetrics()
    }

    private abstract class CustomTaskA : Task
    private abstract class CustomTaskB : Task
}
