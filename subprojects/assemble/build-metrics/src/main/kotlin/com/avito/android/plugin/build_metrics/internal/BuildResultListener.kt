package com.avito.android.plugin.build_metrics.internal

import com.avito.android.gradle.profile.BuildProfile

// TODO: migrate to a build operations listener MBS-11256
internal interface BuildResultListener {

    val name: String
    fun onBuildFinished(status: BuildStatus, profile: BuildProfile)
}
