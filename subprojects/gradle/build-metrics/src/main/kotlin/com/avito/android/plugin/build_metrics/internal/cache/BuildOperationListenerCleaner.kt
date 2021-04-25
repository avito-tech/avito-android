package com.avito.android.plugin.build_metrics.internal.cache

import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.plugin.build_metrics.internal.buildOperationListenerManager
import org.gradle.BuildResult

internal class BuildOperationListenerCleaner(
    private val listener: BuildCacheOperationListener
) : AbstractBuildEventsListener() {

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        val gradle = buildResult.gradle ?: return
        gradle.buildOperationListenerManager().removeListener(listener)
    }
}
