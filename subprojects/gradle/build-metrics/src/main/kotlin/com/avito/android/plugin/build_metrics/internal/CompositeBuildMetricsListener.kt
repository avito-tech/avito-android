package com.avito.android.plugin.build_metrics.internal

import com.avito.android.build_metrics.BuildStatus
import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import org.gradle.BuildResult

internal class CompositeBuildMetricsListener(
    private val listeners: List<BuildResultListener>,
) : AbstractBuildEventsListener() {

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        if (!isRealBuild(buildResult)) return

        val status = if (buildResult.failure == null) BuildStatus.Success else BuildStatus.Fail

        listeners.forEach {
            it.onBuildFinished(status, profile)
        }
    }

    private fun isRealBuild(buildResult: BuildResult): Boolean {
        if (!buildResult.isBuildAction()) return false

        if (buildResult.gradle?.startParameter?.isDryRun == true) {
            return false
        }
        return true
    }

    private fun BuildResult.isBuildAction(): Boolean {
        return action == "Build"
    }
}
