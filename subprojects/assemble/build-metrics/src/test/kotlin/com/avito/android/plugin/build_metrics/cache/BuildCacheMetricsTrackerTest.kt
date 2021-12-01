package com.avito.android.plugin.build_metrics.cache

import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.plugin.build_metrics.internal.BuildCacheOperationType.LOAD
import com.avito.android.plugin.build_metrics.internal.BuildCacheOperationType.STORE
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.RemoteBuildCacheError
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.cache.BuildCacheMetricsTracker
import com.avito.android.sentry.StubEnvironmentInfo
import com.avito.android.stats.CountMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsMetric
import com.avito.android.stats.StubStatsdSender
import com.avito.utils.gradle.Environment
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Task
import org.gradle.util.Path
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class BuildCacheMetricsTrackerTest {

    private val envInfo = StubEnvironmentInfo(
        node = "user",
        environment = Environment.Local
    )

    @Test
    fun `sends - remote hit-miss count`() {
        val metrics = processResults(
            BuildOperationsResult(
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
                        path = ":lib:nonCacheable",
                        type = CustomTask::class.java,
                        cacheResult = TaskCacheResult.Disabled
                    )
                ),
                cacheOperations = CacheOperations(
                    errors = emptyList()
                )
            )
        )
        assertThat(metrics).contains(
            CountMetric(
                SeriesName.create("local", "user", "build", "cache", "remote", "hit"),
                delta = 1
            )
        )
        assertThat(metrics).contains(
            CountMetric(
                SeriesName.create("local", "user", "build", "cache", "remote", "miss"),
                delta = 1
            )
        )
    }

    @Test
    fun `sends - caching errors`() {
        val metrics = processResults(
            BuildOperationsResult(
                tasksExecutions = listOf(
                    taskExecution(
                        path = ":lib:build",
                        type = CustomTask::class.java,
                        cacheResult = TaskCacheResult.Miss(local = true, remote = true)
                    )
                ),
                cacheOperations = CacheOperations(
                    errors = listOf(
                        RemoteBuildCacheError(
                            type = LOAD,
                            httpStatus = 503,
                            cause = RuntimeException("unknown")
                        ),
                        RemoteBuildCacheError(
                            type = STORE,
                            httpStatus = 500,
                            cause = RuntimeException("unknown")
                        )
                    )
                )
            )
        )
        assertThat(metrics).contains(
            CountMetric(
                SeriesName.create("local", "user", "build", "cache", "errors", "load", "503")
            )
        )
        assertThat(metrics).contains(
            CountMetric(
                SeriesName.create("local", "user", "build", "cache", "errors", "store", "500")
            )
        )
    }

    private fun taskExecution(
        path: String,
        type: Class<out Task>,
        startMs: Long = 0,
        endMs: Long = 1,
        cacheResult: TaskCacheResult
    ) = TaskExecutionResult(
        path = Path.path(path),
        type = type,
        startMs = startMs,
        endMs = endMs,
        cacheResult = cacheResult
    )

    private fun processResults(result: BuildOperationsResult): List<StatsMetric> {
        val statsd = StubStatsdSender()
        val metricsTracker = BuildMetricTracker(envInfo, statsd)

        val listener = BuildCacheMetricsTracker(
            metricsTracker, LoggerFactory.getLogger(BuildCacheMetricsTracker::class.java)
        )
        listener.onBuildFinished(result)

        return statsd.getSentMetrics()
    }

    private abstract class CustomTask : Task
}
