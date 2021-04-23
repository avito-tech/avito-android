package com.avito.android.plugin.build_metrics.internal.cache

import com.avito.android.gradle.metric.BuildEventsListener
import com.avito.android.gradle.metric.NoOpBuildEventsListener
import com.avito.android.plugin.build_metrics.internal.AbstractBuildOperationListener
import com.avito.android.plugin.build_metrics.internal.buildOperationListenerManager
import com.avito.android.stats.CountMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.statsd
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.provider.Provider
import org.gradle.caching.http.HttpBuildCache
import org.gradle.caching.internal.controller.operations.LoadOperationDetails
import org.gradle.caching.internal.controller.operations.StoreOperationDetails
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.OperationFinishEvent

internal class BuildCacheOperationListener(
    private val statsd: Provider<StatsDSender>,
) : AbstractBuildOperationListener() {

    private val cacheErrorMetricPrefix = SeriesName.create("build", "cache", "errors")

    override fun finished(descriptor: BuildOperationDescriptor, event: OperationFinishEvent) {
        val failure = event.failure

        if (failure != null) {
            if (descriptor.isLoadOperation()) {
                trackCacheLoadFailure(failure)
            }
            if (descriptor.isStoreOperation()) {
                trackCacheStoreFailure(failure)
            }
        }
    }

    private fun trackCacheLoadFailure(failure: Throwable) {
        val status: String = extractLoadFailureStatusCode(failure) ?: "unknown"

        val prefix = cacheErrorMetricPrefix
            .append("load")
            .append(status)

        statsd.get().send(
            CountMetric(prefix)
        )
    }

    private fun trackCacheStoreFailure(failure: Throwable) {
        val status: String = extractStoreFailureStatusCode(failure) ?: "unknown"

        val prefix = cacheErrorMetricPrefix
            .append("store")
            .append(status)

        statsd.get().send(
            CountMetric(prefix)
        )
    }

    private fun BuildOperationDescriptor.isLoadOperation(): Boolean {
        return details is LoadOperationDetails
            && name.startsWith("Load entry")
            && name.contains("remote build cache")
    }

    private fun BuildOperationDescriptor.isStoreOperation(): Boolean {
        return details is StoreOperationDetails
            && name.startsWith("Store entry")
            && name.contains("remote build cache")
    }

    /**
     * Sample:
     * Loading entry from 'http://host/cache/abcdef' response status 500: Server Error
     */
    private fun extractLoadFailureStatusCode(error: Throwable): String? {
        val message = error.message ?: return null

        return if (message.startsWith("Loading entry from")) {
            message.substringAfter(" response status ").substringBefore(':').trim()
        } else {
            null
        }
    }

    /**
     * Sample:
     * Storing entry at 'http://host/cache/abcdef' response status 500: Server Error
     */
    private fun extractStoreFailureStatusCode(error: Throwable): String? {
        val message = error.message ?: return null

        return if (message.startsWith("Storing entry at")) {
            message.substringAfter(" response status ").substringBefore(':').trim()
        } else {
            null
        }
    }

    companion object {

        fun register(project: Project): BuildEventsListener {
            if (!canTrackRemoteCache(project)) return NoOpBuildEventsListener()

            val buildOperationListener = BuildCacheOperationListener(
                statsd = project.statsd
            )
            project.gradle.buildOperationListenerManager().addListener(buildOperationListener)

            return BuildOperationListenerCleaner(buildOperationListener)
        }

        private fun canTrackRemoteCache(project: Project): Boolean {
            val remoteBuildCache = (project as ProjectInternal).gradle.settings.buildCache.remote
            return remoteBuildCache != null
                && remoteBuildCache.isEnabled
                && remoteBuildCache is HttpBuildCache
        }
    }
}
