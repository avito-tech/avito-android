package com.avito.android.plugin.build_metrics.internal

import com.avito.android.build_metrics.BuildStatus
import com.avito.android.gradle.profile.BuildProfile

// TODO: migrate to a build operations listener MBS-11256
internal interface BuildResultListener {

    fun onBuildFinished(status: BuildStatus, profile: BuildProfile)
}
