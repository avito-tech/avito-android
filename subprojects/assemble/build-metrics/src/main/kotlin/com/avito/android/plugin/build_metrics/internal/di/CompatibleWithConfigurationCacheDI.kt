package com.avito.android.plugin.build_metrics.internal.di

import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultProvider
import com.avito.android.plugin.build_metrics.internal.BuildResultListener
import com.avito.android.plugin.build_metrics.internal.CompositeBuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.AppBuildTimeListener
import com.avito.android.plugin.build_metrics.internal.gradle.cache.BuildCacheMetricsTracker
import com.avito.android.plugin.build_metrics.internal.gradle.configuration.ConfigurationTimeListener
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile.CompileMetricsTracker
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.slow.SlowTasksMetricsTracker
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.tech_budget.TechBudgetBuildTimeWriter
import com.avito.android.plugin.build_metrics.internal.gradle.total_build.TotalBuildTimeListener
import com.avito.android.plugin.build_metrics.internal.runtime.MetricsCollector
import com.avito.android.plugin.build_metrics.internal.runtime.RuntimeMetricsListener
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.JavaHome
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.JvmMetricsCollector
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.JvmMetricsSenderImpl
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.VmResolver
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.command.Jcmd
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.command.Jps
import com.avito.android.plugin.build_metrics.internal.runtime.os.Cgroup2
import com.avito.android.plugin.build_metrics.internal.runtime.os.OsMetricsCollector
import com.avito.android.plugin.build_metrics.internal.runtime.os.OsMetricsSenderImpl
import com.avito.utils.ProcessRunner

internal class CompatibleWithConfigurationCacheDI(
    private val parameters: BuildOperationsResultProvider.Params
) {

    private val loggerFactory = parameters.loggerService
        .zip(parameters.loggerCoordinates) { service, coordinates ->
            service.createLoggerFactory(coordinates)
        }.get()

    private val sender =
        BuildMetricsSenderProvider(
            buildType = parameters.buildType.get(),
            environment = parameters.environment.get(),
            statsDConfig = parameters.statsdConfig.get(),
            graphiteConfig = parameters.graphiteConfig.get(),
            isTest = parameters.test.get(),
            loggerFactory = loggerFactory
        ).provide()

    fun createBuildResultListener(): List<BuildResultListener> {
        return buildList {
            val runtimeMetricsCollectors = createRuntimeMetricsCollectors(parameters)
            if (runtimeMetricsCollectors.isNotEmpty()) {
                add(RuntimeMetricsListener(runtimeMetricsCollectors))
            }
            if (parameters.sendBuildInitConfiguration.get()) {
                add(ConfigurationTimeListener(sender))
            }

            if (parameters.sendBuildTotal.get()) {
                add(TotalBuildTimeListener(sender))
            }
        }
    }

    private fun createRuntimeMetricsCollectors(
        parameters: BuildOperationsResultProvider.Params,
    ): List<MetricsCollector> {
        return buildList {
            if (parameters.sendJvmMetrics.get()) {
                val javaHome = JavaHome()
                val processRunner = ProcessRunner.create(workingDirectory = null)
                add(
                    JvmMetricsCollector(
                        vmResolver = VmResolver(
                            jps = Jps(processRunner, javaHome)
                        ), jcmd = Jcmd(processRunner, javaHome), sender = JvmMetricsSenderImpl(sender, loggerFactory)
                    )
                )
            }
            if (parameters.sendOsMetrics.get()) {
                add(
                    OsMetricsCollector(
                        cgroup = Cgroup2.resolve(), sender = OsMetricsSenderImpl(sender)
                    )
                )
            }
        }
    }

    fun createListener(): BuildOperationsResultListener {
        val listeners: List<BuildOperationsResultListener> = buildList {
            if (parameters.sendCompileMetrics.get()) {
                add(CompileMetricsTracker(sender, parameters.compileMetricsMinimumDuration.get()))
            }
            if (parameters.sendSlowTaskMetrics.get()) {
                add(
                    SlowTasksMetricsTracker(
                        metricsTracker = sender, minimumDuration = parameters.slowTaskMinimumDuration.get()
                    )
                )
            }
            if (parameters.sendBuildCacheMetrics.get() && parameters.canTrackRemoteCache.get()) {
                add(
                    BuildCacheMetricsTracker(
                        metricsTracker = sender,
                        observableTaskTypes = parameters.buildCacheObservableTasks.get(),
                        loggerFactory = loggerFactory,
                    )
                )
            }

            if (parameters.writeModulesBuildTime.get()) {
                add(
                    TechBudgetBuildTimeWriter(
                        fileToWrite = parameters.modulesBuildTimeFile,
                    )
                )
            }
            if (parameters.sendAppBuildTime.get()) {
                add(AppBuildTimeListener(sender))
            }
        }

        return CompositeBuildOperationsResultListener(
            listeners,
            loggerFactory,
        )
    }
}
