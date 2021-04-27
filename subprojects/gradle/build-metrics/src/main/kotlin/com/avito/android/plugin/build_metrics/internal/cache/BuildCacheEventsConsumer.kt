package com.avito.android.plugin.build_metrics.internal.cache

import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.CountMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import org.gradle.api.provider.Provider

internal interface BuildCacheEventsConsumer {

    fun onCacheLoadError(httpStatus: Int?)

    fun onCacheStoreError(httpStatus: Int?)

    fun onBuildFinished(tasksExecutions: List<TaskExecutionResult>)
}

internal class BuildCacheEventsConsumerImpl(
    private val statsd: Provider<StatsDSender>,
    private val environmentInfo: Provider<EnvironmentInfo>
) : BuildCacheEventsConsumer {

    override fun onCacheLoadError(httpStatus: Int?) {
        val name = SeriesName
            .create("build.cache.errors.load", multipart = true)
            .append(httpStatus?.toString() ?: "unknown")

        statsd.get().send(CountMetric(name))
    }

    override fun onCacheStoreError(httpStatus: Int?) {
        val name = SeriesName
            .create("build.cache.errors.store", multipart = true)
            .append(httpStatus?.toString() ?: "unknown")

        statsd.get().send(CountMetric(name))
    }

    override fun onBuildFinished(tasksExecutions: List<TaskExecutionResult>) {
        trackRemoteCacheStats(tasksExecutions)
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
