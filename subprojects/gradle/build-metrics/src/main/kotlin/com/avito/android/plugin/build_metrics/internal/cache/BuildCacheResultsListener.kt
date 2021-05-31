package com.avito.android.plugin.build_metrics.internal.cache

import com.avito.android.plugin.build_metrics.internal.BuildCacheOperationType.LOAD
import com.avito.android.plugin.build_metrics.internal.BuildCacheOperationType.STORE
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.CountMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.logger.GradleLoggerFactory
import org.gradle.api.provider.Provider

internal class BuildCacheResultsListener(
    private val statsd: Provider<StatsDSender>,
    private val environmentInfo: Provider<EnvironmentInfo>,
    loggerFactory: GradleLoggerFactory
) : BuildOperationsResultListener {

    private val logger = loggerFactory.create(BuildCacheResultsListener::class.java.simpleName)

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
                logger.warn("Unknown cache $operationType error", error.cause)
            }
            val metricName = if (error.httpStatus != null) {
                SeriesName
                    .create("build", "cache", "errors", operationType, error.httpStatus.toString())
            } else {
                SeriesName
                    .create("build", "cache", "errors", operationType, "unknown")
            }
            statsd.get().send(CountMetric(metricName))
        }
    }

    private fun trackRemoteCacheStats(tasksExecutions: List<TaskExecutionResult>) {
        val prefix = SeriesName.create("build.cache.remote", multipart = true)

        val remoteHits = tasksExecutions
            .count { it.cacheResult is TaskCacheResult.Hit.Remote }

        val remoteMisses = tasksExecutions
            .count { it.cacheResult is TaskCacheResult.Miss && it.cacheResult.remote }

        statsd.get().send(
            CountMetric(
                prefix.append("hit", "env", environmentInfo.get().environment.publicName),
                remoteHits.toLong()
            )
        )
        statsd.get().send(
            CountMetric(
                prefix.append("miss", "env", environmentInfo.get().environment.publicName),
                remoteMisses.toLong()
            )
        )
    }
}
