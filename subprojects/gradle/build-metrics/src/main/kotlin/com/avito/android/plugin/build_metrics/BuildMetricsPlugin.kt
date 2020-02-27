package com.avito.android.plugin.build_metrics

import com.avito.android.gradle.profile.ProfileEventAdapter
import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.statsd
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import com.avito.utils.logging.ciLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.buildevents.BuildStartedTime
import org.gradle.internal.logging.LoggingOutputInternal
import org.gradle.internal.time.Clock
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.File

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
        configureCollectors(project, listOfNotNull(
            aggregatedConsumer(project),
            SentryConsumer(project),
            buildTraceConsumer(project)
        ))
        project.tasks.register("collectTeamcityMetrics", CollectTeamcityMetricsTask::class.java)
    }

    private fun aggregatedConsumer(project: Project): MetricsConsumer {
        val graphiteClient = BuildMetricTracker(project.environmentInfo(), project.statsd)
        return AggregatedMetricsConsumer(project, graphiteClient)
    }

    private fun buildTraceConsumer(project: Project): MetricsConsumer? {
        return if (isBuildTraceEnabled(project)) {
            BuildTraceConsumer(
                output = File(project.projectDir, "outputs/trace/build.trace"),
                logger = project.ciLogger
            )
        } else {
            null
        }
    }

    private fun isBuildTraceEnabled(project: Project): Boolean {
        return (project.buildEnvironment is BuildEnvironment.CI)
            || (project.gradle.startParameter.isBuildScan)
            || (project.gradle.startParameter.isProfile)
            || (project.getBooleanProperty("android.enableProfileJson", default = false))
    }

    private fun configureCollectors(project: Project, consumers: List<MetricsConsumer>) {
        val collector = GradleCollector(consumers)

        val gradle = project.gradle
        val clock = gradle.serviceOf<Clock>()
        val startedTime = gradle.serviceOf<BuildStartedTime>()
        val adapter = ProfileEventAdapter(clock, collector)
        adapter.buildStarted(startedTime)

        gradle.addListener(adapter)

        val outputService = gradle.serviceOf<LoggingOutputInternal>()
        outputService.addStandardOutputListener(collector)
    }
}
