package com.avito.android.plugin.build_metrics

import com.avito.android.clickstream.config.clickStreamConfig
import com.avito.android.critical_path.CriticalPathRegistry
import com.avito.android.graphite.graphiteConfig
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultProvider
import com.avito.android.plugin.build_metrics.internal.di.NotCompatibleWithConfigurationCacheDI
import com.avito.android.plugin.build_metrics.internal.di.NotCompatibleWithConfigurationCacheDI.Companion.isTestProperty
import com.avito.android.plugin.build_metrics.internal.result.BuildResultFlowAction
import com.avito.android.stats.statsdConfig
import com.avito.git.gitStateProvider
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerCoordinates
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.configuration.BuildFeatures
import org.gradle.api.flow.FlowProviders
import org.gradle.api.flow.FlowScope
import org.gradle.internal.build.event.BuildEventListenerRegistryInternal
import org.gradle.kotlin.dsl.create
import javax.inject.Inject

public abstract class BuildMetricsPlugin : Plugin<Project> {

    @get:Inject
    internal abstract val buildEventListenerRegistryInternal: BuildEventListenerRegistryInternal

    @Suppress("UnstableApiUsage")
    @get:Inject
    internal abstract val flowScope: FlowScope

    @Suppress("UnstableApiUsage")
    @get:Inject
    internal abstract val flowProviders: FlowProviders

    @get:Inject
    internal abstract val buildFeatures: BuildFeatures

    override fun apply(project: Project) {
        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }

        val extension = project.extensions.create<BuildMetricsExtension>("buildMetrics").apply {
            branchName.convention(project.gitStateProvider().map { it.currentBranch.name })
            repoName.convention(
                project.getOptionalStringProperty(
                    "avito.bitbucket.repositorySlug",
                    default = "",
                )
            )
        }

        if (!project.pluginIsEnabled) {
            project.logger.lifecycle("Build metrics plugin is disabled")
            return
        }

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
                val collector = project
                    .gradle
                    .sharedServices
                    .registerIfAbsent("bmp", BuildOperationsResultProvider::class.java) {
                        with(it.parameters) {
                            loggerService.set(GradleLoggerPlugin.getLoggerService(project))
                            loggerCoordinates.set(GradleLoggerCoordinates(project.path))
                            test.set(project.hasProperty(isTestProperty))
                            buildType.set(extension.buildType)
                            userName.set(extension.userName)
                            environment.set(extension.environment)
                            statsdConfig.set(project.statsdConfig)
                            graphiteConfig.set(project.graphiteConfig)
                            clickStreamConfig.set(project.clickStreamConfig)
                            sendCompileMetrics.set(extension.sendCompileMetrics)
                            compileMetricsMinimumDuration.set(extension.compileMetricsMinimumDuration)
                            sendSlowTaskMetrics.set(extension.sendSlowTaskMetrics)
                            slowTaskMinimumDuration.set(extension.slowTaskMinimumDuration)
                            sendBuildCacheMetrics.set(extension.sendBuildCacheMetrics)
                            canTrackRemoteCache.set(BuildOperationsResultProvider.canTrackRemoteCache(project))
                            buildCacheObservableTasks.set(extension.buildCacheObservableTasks)
                            writeModulesBuildTime.set(extension.writeModulesBuildTime)
                            modulesBuildTimeFile.set(extension.modulesBuildTimeFile)
                            sendJvmMetrics.set(extension.sendJvmMetrics)
                            sendOsMetrics.set(extension.sendOsMetrics)
                            sendBuildInitConfiguration.set(extension.sendBuildInitConfiguration)
                            sendBuildTotal.set(extension.sendBuildTotal)
                            sendAppBuildTime.set(extension.sendAppBuildTime)
                            sendTestRunnerMetrics.set(extension.sendTestRunnerMetrics)
                            branchName.set(extension.branchName)
                            repoName.set(extension.repoName)
                        }
                    }

                @Suppress("UnstableApiUsage")
                flowScope.always(BuildResultFlowAction::class.java) {
                    it.parameters.buildWorkResult.set(flowProviders.buildWorkResult)
                }

                buildEventListenerRegistryInternal.onOperationCompletion(collector)

                if (!buildFeatures.configurationCache.active.get()) {
                    val di = NotCompatibleWithConfigurationCacheDI(
                        project,
                        extension,
                        GradleLoggerPlugin.getLoggerFactory(project)
                    )
                    registerNotCompatibleWithCCListeners(di, extension)
                }
            }
        }
    }

    private fun registerNotCompatibleWithCCListeners(
        di: NotCompatibleWithConfigurationCacheDI,
        extension: BuildMetricsExtension,
    ) {
        if (extension.sendCriticalPathMetrics.get()) {
            CriticalPathRegistry.addListener(di.project, di.criticalPathTracker)
        }
    }
}

internal val Project.pluginIsEnabled: Boolean
    get() = providers
        .gradleProperty(enabledProp)
        .map { it.toBoolean() }
        .getOrElse(false)

private const val enabledProp = "avito.build.metrics.enabled"
