package com.avito.impact.plugin.internal

import com.avito.android.isAndroidApp
import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.impact.ModifiedProject
import com.avito.impact.ModifiedProjectsFinder
import com.avito.math.percentOf
import com.avito.module.configurations.ConfigurationType
import com.avito.utils.gradle.Environment
import org.gradle.api.Project
import java.util.Locale

class ImpactMetricsSender(
    private val projectsFinder: ModifiedProjectsFinder,
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
        SeriesName.create(envName, node, buildId, "impact")
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

    fun sendMetrics() {
        val allProjects = projectsFinder.allProjects()

        ConfigurationType.values().forEach { type ->
            val modified = projectsFinder.modifiedProjects(type)

            sendModulesMetrics(type, allProjects, modified)
            sendAppsMetrics(type, allProjects, modified)
        }
    }

    private fun sendModulesMetrics(
        configurationType: ConfigurationType,
        projects: Set<Project>,
        modified: Set<ModifiedProject>
    ) {
        sendMetric(
            metric = SeriesName.create("modules", configurationType.name.toLowerCase(Locale.US), "modified"),
            value = modified.size.percentOf(projects.size).toLong()
        )
    }

    private fun sendAppsMetrics(
        configurationType: ConfigurationType,
        projects: Set<Project>,
        modified: Set<ModifiedProject>
    ) {
        val apps = projects.count { it.isAndroidApp() }
        val modifiedApps = modified.count { it.project.isAndroidApp() }
        sendMetric(
            metric = SeriesName.create("apps", configurationType.name.toLowerCase(Locale.US), "modified"),
            value = modifiedApps.percentOf(apps).toLong()
        )
    }

    @Suppress("DefaultLocale")
    private fun sendMetric(metric: SeriesName, value: Long) {
        statsDSender.send(
            GaugeLongMetric(prefix.append(metric), value)
        )
    }
}
