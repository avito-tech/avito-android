package com.avito.impact.plugin

import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.graphiteSeries
import com.avito.impact.ConfigurationType

class ImpactMetricsSender(
    private val statsDSender: StatsDSender,
    private val environmentInfo: EnvironmentInfo
) {

    private val prefix by lazy {
        val envName = environmentInfo.environment.publicName
        val node = environmentInfo.node?.take(32) ?: "_"
        val buildId = environmentInfo.teamcityBuildId() ?: "_"
        graphiteSeries(envName, node, buildId)
    }

    @Suppress("DefaultLocale")
    fun send(type: ConfigurationType, name: String, value: Number) {
        statsDSender.send(
            prefix,
            GaugeMetric(graphiteSeries("impact", type.name.toLowerCase(), name), value)
        )
    }
}
