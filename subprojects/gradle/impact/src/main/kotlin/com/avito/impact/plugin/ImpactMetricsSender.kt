package com.avito.impact.plugin

import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.graphiteSeries
import com.avito.impact.ConfigurationType
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.configuration.internalModule

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

    fun sendModifiedProjectCounters(modifiedProjectsFinder: ModifiedProjectsFinder) {
        modifiedProjectsFinder.getProjects().forEach { (type, projects) ->
            val modified = projects.filter { it.project.internalModule.isModified(type) }

            sendMetric(type, "all", projects.size)
            sendMetric(type, "modified", modified.size)
        }
    }

    @Suppress("DefaultLocale")
    private fun sendMetric(type: ConfigurationType, name: String, value: Number) {
        statsDSender.send(
            prefix,
            GaugeMetric(graphiteSeries("impact", type.name.toLowerCase(), name), value)
        )
    }
}
