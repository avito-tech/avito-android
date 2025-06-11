package com.avito.android.plugin.build_metrics.internal.gradle.total_build

import com.avito.android.plugin.build_metrics.internal.BuildResultListener
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.result.BuildResult

internal class TotalBuildTimeListener(
    private val metricTracker: BuildMetricSender
) : BuildResultListener {

    override val name: String = "TotalBuildTime"

    override fun onBuildFinished(result: BuildResult) {
        metricTracker.send(BuildTotalMetric(result.buildDuration.toMillis()))
    }
}
