package com.avito.android.plugin.build_metrics

import com.avito.android.gradle.metric.GradleCollector
import com.avito.android.gradle.metric.MetricsConsumer
import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.statsd
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Inspired by [gradle-metrics-plugin](https://github.com/nebula-plugins/gradle-metrics-plugin)
 */
open class BuildMetricsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }
        if (!project.getBooleanProperty("avito.build.metrics.enabled", default = false)) {
            project.logger.lifecycle("Build metrics plugin is disabled")
            return
        }
        GradleCollector.initialize(
            project,
            listOf(
                aggregatedConsumer(project),
                SentryConsumer(project)
            )
        )
        project.tasks.register<CollectTeamcityMetricsTask>("collectTeamcityMetrics") {
            buildId.set(project.getOptionalStringProperty("avito.build.metrics.teamcityBuildId"))
        }
    }

    private fun aggregatedConsumer(project: Project): MetricsConsumer {
        val graphiteClient = BuildMetricTracker(project.environmentInfo(), project.statsd)
        return AggregatedMetricsConsumer(project, graphiteClient)
    }

}
