package com.avito.android.plugin.build_metrics.internal.gradle.configuration

import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.plugin.build_metrics.internal.BuildResultListener
import com.avito.android.plugin.build_metrics.internal.BuildStatus
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender

internal class ConfigurationTimeListener(
    private val sender: BuildMetricSender
) : BuildResultListener {
    override val name: String = "ConfigurationTime"

    override fun onBuildFinished(status: BuildStatus, profile: BuildProfile) {
        val metric = BuildInitConfigurationMetric(profile.initWithConfigurationTimeMs)
        sender.send(metric)
    }
}
