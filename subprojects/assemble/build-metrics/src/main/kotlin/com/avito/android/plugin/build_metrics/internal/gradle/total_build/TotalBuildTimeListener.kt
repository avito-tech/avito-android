package com.avito.android.plugin.build_metrics.internal.gradle.total_build

import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.plugin.build_metrics.internal.BuildResultListener
import com.avito.android.plugin.build_metrics.internal.BuildStatus
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender

internal class TotalBuildTimeListener(
    private val metricTracker: BuildMetricSender
) : BuildResultListener {

    override fun onBuildFinished(status: BuildStatus, profile: BuildProfile) {
        metricTracker.send(BuildTotalMetric(profile.elapsedTotal))
    }
}
