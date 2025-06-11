package com.avito.android.plugin.build_metrics.internal.di

import com.avito.android.graphite.graphiteConfig
import com.avito.android.plugin.build_metrics.BuildMetricsExtension
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.critical.CriticalPathMetricsTracker
import com.avito.android.stats.statsdConfig
import com.avito.logger.LoggerFactory
import org.gradle.api.Project

internal class NotCompatibleWithConfigurationCacheDI(
    val project: Project,
    private val extension: BuildMetricsExtension,
    private val loggerFactory: LoggerFactory,
) {

    private val sender by lazy {
        BuildMetricsSenderProvider(
            buildType = extension.buildType.get(),
            environment = extension.environment.get(),
            statsDConfig = project.statsdConfig.get(),
            graphiteConfig = project.graphiteConfig.get(),
            isTest = project.hasProperty(isTestProperty),
            loggerFactory = loggerFactory
        ).provide()
    }

    val criticalPathTracker: CriticalPathMetricsTracker by lazy {
        CriticalPathMetricsTracker(sender, extension.criticalTaskMinimumDuration.get())
    }

    companion object {
        internal const val isTestProperty = "build.metrics.test"
    }
}
