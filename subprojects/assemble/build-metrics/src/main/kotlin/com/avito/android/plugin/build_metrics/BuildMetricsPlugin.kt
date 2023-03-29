package com.avito.android.plugin.build_metrics

import com.avito.android.critical_path.CriticalPathRegistry
import com.avito.android.gradle.metric.GradleCollector
import com.avito.android.graphite.graphiteConfig
import com.avito.android.plugin.build_metrics.internal.BuildMetricsPluginDI
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultProvider
import com.avito.android.plugin.build_metrics.internal.BuildResultListener
import com.avito.android.plugin.build_metrics.internal.CompositeBuildMetricsListener
import com.avito.android.plugin.build_metrics.internal.logger.LazyLoggerFactory
import com.avito.android.plugin.build_metrics.internal.runtime.MetricsCollector
import com.avito.android.plugin.build_metrics.internal.runtime.RuntimeMetricsListener
import com.avito.android.plugin.build_metrics.internal.teamcity.CollectTeamcityMetricsTask
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerPlugin
import com.avito.teamcity.teamcityCredentials
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public open class BuildMetricsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }

        val extension = project.extensions.create<BuildMetricsExtension>("buildMetrics")

        if (!project.pluginIsEnabled) {
            project.logger.lifecycle("Build metrics plugin is disabled")
            return
        }

        project.tasks.register<CollectTeamcityMetricsTask>("collectTeamcityMetrics") {
            buildId.set(project.getOptionalStringProperty("avito.build.metrics.teamcityBuildId"))
            this.teamcityCredentials.set(project.teamcityCredentials)
            this.graphiteConfig.set(project.graphiteConfig)
        }
        // values from extension are not available earlier
        project.afterEvaluate {
            if (!extension.buildType.isPresent || !extension.environment.isPresent) {
                project.logger.warn(
                    """
                    Build metrics plugin configuration error. Plugin can't work.
                    Please configure buildType and environment at buildMetrics extension
                """.trimIndent()
                )
            } else if (extension.writeModulesBuildTime.get() && !extension.modulesBuildTimeFile.isPresent) {
                project.logger.warn(
                    """
                    Build metrics plugin configuration error. 
                    writeModulesBuildTime is enabled, but modulesBuildTimeFile location is not specified.
                """.trimIndent()
                )
            } else {
                val di = BuildMetricsPluginDI(
                    project,
                    extension,
                    LazyLoggerFactory(GradleLoggerPlugin.getLoggerFactory(project))
                )
                registerListeners(di, extension)
            }
        }
    }

    private fun registerListeners(
        di: BuildMetricsPluginDI,
        extension: BuildMetricsExtension,
    ) {
        if (extension.sendCriticalPathMetrics.get()) {
            CriticalPathRegistry.addListener(di.project, di.criticalPathTracker)
        }

        val buildResultListeners = createBuildResultListeners(extension, di)
        val buildOperationResultListeners = createBuildOperationsResultListeners(extension, di)

        val eventListeners = buildList {
            if (buildResultListeners.isNotEmpty()) {
                add(CompositeBuildMetricsListener(buildResultListeners))
            }
            if (buildOperationResultListeners.isNotEmpty()) {
                add(BuildOperationsResultProvider.register(di.project, buildOperationResultListeners))
            }
        }
        if (eventListeners.isNotEmpty()) {
            GradleCollector.initialize(
                di.project,
                eventListeners
            )
        }
    }

    private fun createBuildOperationsResultListeners(
        extension: BuildMetricsExtension,
        di: BuildMetricsPluginDI
    ): List<BuildOperationsResultListener> {
        return buildList {
            if (extension.sendSlowTaskMetrics.get()) {
                add(di.slowTasksMetricsTracker)
            }
            if (extension.sendBuildCacheMetrics.get() &&
                BuildOperationsResultProvider.canTrackRemoteCache(di.project)
            ) {
                add(di.cacheMetricsTracker)
            }
            if (extension.writeModulesBuildTime.get()) {
                add(di.techBudgetBuildTimeWriter)
            }
        }
    }

    private fun createBuildResultListeners(
        extension: BuildMetricsExtension,
        di: BuildMetricsPluginDI
    ): List<BuildResultListener> {
        return buildList {
            val runtimeMetricsCollectors = createRuntimeMetricsCollectors(extension, di)
            if (runtimeMetricsCollectors.isNotEmpty()) {
                add(RuntimeMetricsListener(runtimeMetricsCollectors))
            }
            if (extension.sendBuildInitConfiguration.get()) {
                add(di.initConfigurationListener)
            }
            if (extension.sendBuildTotal.get()) {
                add(di.totalBuildTimeListener)
            }
            if (extension.sendAppBuildTime.get()) {
                add(di.appBuildTimeListener)
            }
        }
    }

    private fun createRuntimeMetricsCollectors(
        extension: BuildMetricsExtension,
        di: BuildMetricsPluginDI
    ): List<MetricsCollector> {
        return buildList {
            if (extension.sendJvmMetrics.get()) {
                add(di.jvmMetricsCollector)
            }
            if (extension.sendOsMetrics.get()) {
                add(di.osMetricsCollector)
            }
        }
    }
}

internal val Project.pluginIsEnabled: Boolean
    get() = providers
        .gradleProperty(enabledProp)
        .map { it.toBoolean() }
        .getOrElse(false)

private const val enabledProp = "avito.build.metrics.enabled"
