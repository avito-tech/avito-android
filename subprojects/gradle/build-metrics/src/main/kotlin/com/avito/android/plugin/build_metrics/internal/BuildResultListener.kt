package com.avito.android.plugin.build_metrics.internal

import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.plugin.build_metrics.BuildMetricTracker.BuildStatus

internal interface BuildResultListener {

    fun onBuildFinished(status: BuildStatus, profile: BuildProfile)
}
