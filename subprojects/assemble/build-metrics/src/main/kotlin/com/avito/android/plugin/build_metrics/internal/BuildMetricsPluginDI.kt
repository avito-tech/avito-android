package com.avito.android.plugin.build_metrics.internal

import com.avito.android.graphite.GraphiteSender
import com.avito.android.graphite.graphiteConfig
import com.avito.android.plugin.build_metrics.BuildMetricsExtension
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.AppBuildTimeListener
import com.avito.android.plugin.build_metrics.internal.gradle.cache.BuildCacheMetricsTracker
import com.avito.android.plugin.build_metrics.internal.gradle.configuration.ConfigurationTimeListener
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.critical.CriticalPathMetricsTracker
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.slow.SlowTasksMetricsTracker
import com.avito.android.plugin.build_metrics.internal.gradle.total_build.TotalBuildTimeListener
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.JavaHome
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.JvmMetricsCollector
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.JvmMetricsSenderImpl
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.VmResolver
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.command.Jcmd
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.command.Jps
import com.avito.android.plugin.build_metrics.internal.runtime.os.Cgroup2
import com.avito.android.plugin.build_metrics.internal.runtime.os.OsMetricsCollector
import com.avito.android.plugin.build_metrics.internal.runtime.os.OsMetricsSenderImpl
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.statsd
import com.avito.android.stats.withPrefix
import com.avito.graphite.series.SeriesName
import com.avito.logger.LoggerFactory
import com.avito.utils.ProcessRunner
import org.gradle.api.Project

internal class BuildMetricsPluginDI(
    val project: Project,
    private val extension: BuildMetricsExtension,
    private val loggerFactory: LoggerFactory,
) {
    private val pluginMetricsPrefix = SeriesName.create("builds")
        .addTag("build_type", extension.buildType.get())
        .addTag("env", extension.environment.get().code)

    private val statsdSender: StatsDSender by lazy {
        project.statsd.get().withPrefix(pluginMetricsPrefix)
    }

    private val graphiteSender by lazy {
        val config = project.graphiteConfig.get()
        val metricPrefix = config.metricPrefix.append(pluginMetricsPrefix)
        GraphiteSender.create(
            config = config.copy(metricPrefix = metricPrefix),
            loggerFactory = loggerFactory,
            isTest = project.hasProperty(isTestProperty)
        )
    }

    private val sender by lazy {
        BuildMetricSender.create(statsdSender, graphiteSender)
    }

    val criticalPathTracker: CriticalPathMetricsTracker by lazy {
        CriticalPathMetricsTracker(sender)
    }

    val jvmMetricsCollector by lazy {
        val javaHome = JavaHome()
        val processRunner = ProcessRunner.create(workingDirectory = null)
        JvmMetricsCollector(
            vmResolver = VmResolver(
                jps = Jps(processRunner, javaHome)
            ),
            jcmd = Jcmd(processRunner, javaHome),
            sender = JvmMetricsSenderImpl(sender, loggerFactory)
        )
    }

    val osMetricsCollector by lazy {
        OsMetricsCollector(
            cgroup = Cgroup2.resolve(),
            sender = OsMetricsSenderImpl(sender)
        )
    }

    val initConfigurationListener by lazy {
        ConfigurationTimeListener(sender)
    }

    val totalBuildTimeListener by lazy {
        TotalBuildTimeListener(sender)
    }

    val appBuildTimeListener by lazy {
        AppBuildTimeListener.from(project, sender)
    }

    val cacheMetricsTracker by lazy {
        BuildCacheMetricsTracker(
            metricsTracker = sender,
            loggerFactory = loggerFactory,
        )
    }

    val slowTasksMetricsTracker by lazy {
        SlowTasksMetricsTracker(
            metricsTracker = sender,
            minimumDuration = extension.slowTaskMinimumDuration.get()
        )
    }

    companion object {
        internal const val isTestProperty = "build.metrics.test"
    }
}
