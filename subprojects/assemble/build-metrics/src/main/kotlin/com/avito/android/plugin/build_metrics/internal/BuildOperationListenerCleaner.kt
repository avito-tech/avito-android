package com.avito.android.plugin.build_metrics.internal

import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import org.gradle.BuildResult
import org.gradle.internal.operations.BuildOperationListener

internal class BuildOperationListenerCleaner(
    private val listener: BuildOperationListener
) : AbstractBuildEventsListener() {

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        val gradle = buildResult.gradle ?: return
        gradle.buildOperationListenerManager().removeListener(listener)
    }
}
