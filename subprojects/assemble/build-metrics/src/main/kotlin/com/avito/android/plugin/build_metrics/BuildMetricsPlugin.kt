package com.avito.android.plugin.build_metrics

import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.critical_path.CriticalPathRegistry
import com.avito.android.gradle.metric.GradleCollector
import com.avito.android.graphite.graphiteConfig
import com.avito.android.plugin.build_metrics.internal.AppBuildTimeListener
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultProvider
import com.avito.android.plugin.build_metrics.internal.CompositeBuildMetricsListener
import com.avito.android.plugin.build_metrics.internal.ConfigurationTimeListener
import com.avito.android.plugin.build_metrics.internal.TotalBuildTimeListener
import com.avito.android.plugin.build_metrics.internal.tasks.CriticalPathMetricsTracker
import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.statsd
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.teamcity.teamcityCredentials
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

public open class BuildMetricsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }

        if (!project.pluginIsEnabled) {
            project.logger.lifecycle("Build metrics plugin is disabled")
            return
        }

        project.tasks.register<CollectTeamcityMetricsTask>("collectTeamcityMetrics") {
            buildId.set(project.getOptionalStringProperty("avito.build.metrics.teamcityBuildId"))
            this.teamcityCredentials.set(project.teamcityCredentials)
            this.graphiteConfig.set(project.graphiteConfig)
        }

        val buildMetricTracker = BuildMetricTracker(
            project.environmentInfo().get(),
            project.statsd.get()
        )

        val buildOperationsListener = BuildOperationsResultProvider.register(project, buildMetricTracker)

        val criticalPathTracker = CriticalPathMetricsTracker(buildMetricTracker)
        CriticalPathRegistry.addListener(project, criticalPathTracker)

        val buildListeners = listOf(
            ConfigurationTimeListener(buildMetricTracker),
            TotalBuildTimeListener(buildMetricTracker),
            AppBuildTimeListener.from(project, buildMetricTracker)
        )

        val eventsListeners = listOf(
            CompositeBuildMetricsListener(
                listeners = buildListeners,
            ),
            buildOperationsListener
        )
        GradleCollector.initialize(
            project,
            eventsListeners
        )
    }
}

internal val Project.pluginIsEnabled: Boolean
    get() = providers
        .gradleProperty(enabledProp)
        .forUseAtConfigurationTime()
        .map { it.toBoolean() }
        .getOrElse(false)

private const val enabledProp = "avito.build.metrics.enabled"
