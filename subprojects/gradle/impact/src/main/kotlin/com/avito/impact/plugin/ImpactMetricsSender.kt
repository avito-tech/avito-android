package com.avito.impact.plugin

import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.configuration.internalModule
import com.avito.module.configurations.ConfigurationType
import com.avito.utils.gradle.Environment

class ImpactMetricsSender(
    private val statsDSender: StatsDSender,
    private val environmentInfo: EnvironmentInfo
) {

    init {
        require(environmentInfo.environment is Environment.CI) {
            "ImpactMetricsSender should run only in CI environment"
        }
    }

    private val prefix by lazy {
        val envName = environmentInfo.environment.publicName

        // Don't need when we have build id. Empty value for backward compatibility in series name
        val node = "_"
        val buildId = requireNotNull(environmentInfo.teamcityBuildId()) {
            "ImpactMetricsSender should run only if teamcityBuildInfo available"
        }
        SeriesName.create(envName, node, buildId)
    }

    private val ConfigurationType.name: String
        get() {
            return when (this) {
                ConfigurationType.AndroidTests -> "androidtests"
                ConfigurationType.Main -> "implementation"
                ConfigurationType.Lint -> "lint"
                ConfigurationType.UnitTests -> "unittests"
            }
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
            GaugeMetric(prefix.append("impact", type.name.toLowerCase(), name), value)
        )
    }
}
