package com.avito.android.plugin.build_metrics.cache

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.BuildCacheOperationType
import com.avito.android.plugin.build_metrics.internal.BuildCacheOperationType.LOAD
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.RemoteBuildCacheError
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.core.StubBuildMetricsSender
import com.avito.android.plugin.build_metrics.internal.gradle.cache.BuildCacheMetricsTracker
import com.avito.graphite.series.SeriesName
import com.avito.logger.PrintlnLoggerFactory
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Task
import org.gradle.util.Path
import org.junit.jupiter.api.Test

internal class BuildCacheMetricsTrackerTest {

    private val buildOperationsResult = BuildOperationsResult(
        tasksExecutions = listOf(
            taskExecution(
                path = ":lib:missRemote",
                type = CustomTask::class.java,
                cacheResult = TaskCacheResult.Miss(local = true, remote = true)
            ),
            taskExecution(
                path = ":lib:hitRemote",
                type = CustomTask::class.java,
                cacheResult = TaskCacheResult.Hit.Remote
            ),
            taskExecution(
                path = ":lib:hitRemote2",
                type = CustomTask::class.java,
                cacheResult = TaskCacheResult.Hit.Remote
            ),
            taskExecution(
                path = ":lib:nonCacheable",
                type = CustomTask::class.java,
                cacheResult = TaskCacheResult.Disabled
            ),
            taskExecution(
                path = ":lib2:missRemote",
                type = CustomTask::class.java,
                cacheResult = TaskCacheResult.Miss(local = true, remote = true)
            ),
            taskExecution(
                path = ":lib2:hitRemote",
                type = CustomTask::class.java,
                cacheResult = TaskCacheResult.Hit.Remote
            ),
            taskExecution(
                path = ":lib2:nonCacheable",
                type = CustomTask::class.java,
                cacheResult = TaskCacheResult.Disabled
            )
        ),
        cacheOperations = CacheOperations(
            errors = listOf(
                RemoteBuildCacheError(
                    selector = RemoteBuildCacheError.Selector(
                        type = LOAD,
                        httpStatus = 503,
                    ),
                    cause = RuntimeException("unknown")
                ),
                RemoteBuildCacheError(
                    selector = RemoteBuildCacheError.Selector(
                        type = BuildCacheOperationType.STORE,
                        httpStatus = 500,
                    ),
                    cause = RuntimeException("unknown")
                )
            )
        )
    )

    @Test
    fun `sends - remote hit-miss count`() {
        val metrics = processResults(buildOperationsResult)
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName.create("gradle.cache.remote.hit", multipart = true),
                "3"
            )
        )
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName.create("gradle.cache.remote.miss", multipart = true),
                "2"
            )
        )
    }

    @Test
    fun `sends - caching errors`() {
        val metrics = processResults(buildOperationsResult)
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName.create("gradle", "cache", "errors")
                    .addTag("operation_type", "load")
                    .addTag("error_type", "503"),
                "1"
            )
        )
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName.create("gradle", "cache", "errors")
                    .addTag("operation_type", "store")
                    .addTag("error_type", "500"),
                "1"
            )
        )
    }

    @Test
    fun `task type cache metrics`() {
        val metrics = processResults(buildOperationsResult)
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName
                    .create("gradle.cache.remote.task.type.hit", multipart = true)
                    .addTag("task_type", "CustomTask"),
                "3"
            )
        )
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName
                    .create("gradle.cache.remote.task.type.miss", multipart = true)
                    .addTag("task_type", "CustomTask"),
                "2"
            )
        )
    }

    private fun taskExecution(
        name: String = "stub",
        path: String,
        type: Class<out Task>,
        startMs: Long = 0,
        endMs: Long = 1,
        cacheResult: TaskCacheResult
    ) = TaskExecutionResult(
        name = name,
        path = Path.path(path),
        type = type,
        startMs = startMs,
        endMs = endMs,
        cacheResult = cacheResult
    )

    private fun processResults(result: BuildOperationsResult): List<GraphiteMetric> {
        val buildMetricSender = StubBuildMetricsSender()

        val listener = BuildCacheMetricsTracker(
            buildMetricSender,
            setOf(CustomTask::class.java.simpleName),
            PrintlnLoggerFactory
        )
        listener.onBuildFinished(result)

        return buildMetricSender.getSentGraphiteMetrics()
    }

    private abstract class CustomTask : Task
}
