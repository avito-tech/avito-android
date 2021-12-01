package com.avito.android.plugin.build_metrics.internal.cache

import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.plugin.build_metrics.internal.BuildCacheOperationType.LOAD
import com.avito.android.plugin.build_metrics.internal.BuildCacheOperationType.STORE
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.stats.CountMetric
import com.avito.android.stats.SeriesName
import org.slf4j.Logger

internal class BuildCacheMetricsTracker(
    private val metricsTracker: BuildMetricTracker,
    private val logger: Logger,
) : BuildOperationsResultListener {

    override fun onBuildFinished(result: BuildOperationsResult) {
        trackCacheErrors(result.cacheOperations)
        trackRemoteCacheStats(result.tasksExecutions)
    }

    private fun trackCacheErrors(operations: CacheOperations) {
        operations.errors.forEach { error ->
            val operationType: String = when (error.type) {
                LOAD -> "load"
                STORE -> "store"
            }
            if (error.httpStatus == null) {
                // TODO handle httpStatus null
                logger.warn("Unknown cache $operationType error", error.cause)
            }
            val metricName = if (error.httpStatus != null) {
                SeriesName
                    .create("cache", "errors", operationType, error.httpStatus.toString())
            } else {
                SeriesName
                    .create("cache", "errors", operationType, "unknown")
            }
            metricsTracker.track(CountMetric(metricName))
        }
    }

    private fun trackRemoteCacheStats(tasksExecutions: List<TaskExecutionResult>) {
        val remoteHits = tasksExecutions
            .count { it.cacheResult is TaskCacheResult.Hit.Remote }

        val remoteMisses = tasksExecutions
            .count { it.cacheResult is TaskCacheResult.Miss && it.cacheResult.remote }

        metricsTracker.track(
            CountMetric(
                SeriesName.create("cache", "remote", "hit"),
                remoteHits.toLong()
            )
        )
        metricsTracker.track(
            CountMetric(
                SeriesName.create("cache", "remote", "miss"),
                remoteMisses.toLong()
            )
        )
    }
}
