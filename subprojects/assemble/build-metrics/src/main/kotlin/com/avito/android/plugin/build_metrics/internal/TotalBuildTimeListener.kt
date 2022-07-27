package com.avito.android.plugin.build_metrics.internal

import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric

internal class TotalBuildTimeListener(
    private val metricTracker: StatsDSender
) : BuildResultListener {

    override fun onBuildFinished(status: BuildStatus, profile: BuildProfile) {
        val name = SeriesName.create("id") // for backward compatibility
            .append(status.asSeriesName())
            .append("build-time", "total")

        metricTracker.send(
            TimeMetric(name, profile.elapsedTotal)
        )
    }
}
