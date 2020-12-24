package com.avito.android.plugin.build_metrics

import com.avito.android.gradle.metric.GradleCollector
import com.avito.android.gradle.metric.MetricsConsumer
import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.statsd
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.LoggerFactory
import com.avito.logger.create
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

        val loggerFactory = GradleLoggerFactory.fromPlugin(this, project)
        val logger = loggerFactory.create<BuildMetricsPlugin>()

        if (!project.getBooleanProperty("avito.build.metrics.enabled", default = false)) {
            logger.debug("Build metrics plugin is disabled")
            return
        }

        GradleCollector.initialize(
            project,
            listOf(
                aggregatedConsumer(project, loggerFactory),
                SentryConsumer(project, loggerFactory)
            )
        )

        project.tasks.register<CollectTeamcityMetricsTask>("collectTeamcityMetrics") {
            buildId.set(project.getOptionalStringProperty("avito.build.metrics.teamcityBuildId"))
        }
    }

    private fun aggregatedConsumer(project: Project, loggerFactory: LoggerFactory): MetricsConsumer {
        val graphiteClient = BuildMetricTracker(project.environmentInfo(), project.statsd)
        return AggregatedMetricsConsumer(
            project = project,
            stats = graphiteClient,
            loggerFactory = loggerFactory
        )
    }
}
