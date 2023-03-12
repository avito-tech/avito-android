package com.avito.android.plugin.build_metrics.internal.gradle.cache

import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.logger.LoggerFactory
import com.avito.logger.create

internal class BuildCacheMetricsTracker(
    private val metricsTracker: BuildMetricSender,
    loggerFactory: LoggerFactory,
) : BuildOperationsResultListener {

    private val logger = loggerFactory.create<BuildCacheMetricsTracker>()

    override fun onBuildFinished(result: BuildOperationsResult) {
        trackCacheErrors(result.cacheOperations)
        trackRemoteCacheStats(result.tasksExecutions)
    }

    private fun trackCacheErrors(operations: CacheOperations) {
        operations.errors.groupBy { it.selector }.forEach { (groupSelector, errors) ->
            metricsTracker.send(BuildCacheErrorMetric(groupSelector, errors.size.toLong()))
            if (groupSelector.httpStatus == null) {
                errors.forEach { error ->
                    // TODO handle httpStatus null
                    logger.warn("Unknown cache ${error.selector.type} error", error.cause)
                }
            }
        }
    }

    private fun trackRemoteCacheStats(tasksExecutions: List<TaskExecutionResult>) {
        val remoteHits = tasksExecutions
            .count { it.cacheResult is TaskCacheResult.Hit.Remote }
            .toLong()

        val remoteMisses = tasksExecutions
            .count { it.cacheResult is TaskCacheResult.Miss && it.cacheResult.remote }
            .toLong()

        metricsTracker.send(
            BuildCacheHitMetric(remoteHits)
        )
        metricsTracker.send(
            BuildCacheMissMetric(remoteMisses)
        )
    }
}
