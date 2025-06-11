package com.avito.android.plugin.build_metrics.internal.gradle.configuration

import com.avito.android.plugin.build_metrics.internal.BuildResultListener
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.result.BuildResult

internal class ConfigurationTimeListener(
    private val sender: BuildMetricSender
) : BuildResultListener {

    override val name: String = "ConfigurationTime"

    override fun onBuildFinished(result: BuildResult) {
        val metric = BuildInitConfigurationMetric(result.configurationDuration.toMillis())
        sender.send(metric)
    }
}
