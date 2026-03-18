package com.avito.android.plugin.build_metrics

import com.avito.android.clickstream.config.clickStreamConfig
import com.avito.android.critical_path.CriticalPathRegistry
import com.avito.android.graphite.graphiteConfig
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultProvider
import com.avito.android.plugin.build_metrics.internal.di.NotCompatibleWithConfigurationCacheDI
import com.avito.android.plugin.build_metrics.internal.di.NotCompatibleWithConfigurationCacheDI.Companion.isTestProperty
import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.BuildExecutionHistory
import com.avito.android.plugin.build_metrics.internal.result.BuildResultFlowAction
import com.avito.android.stats.statsdConfig
import com.avito.git.gitStateProvider
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerCoordinates
import com.avito.logger.GradleLoggerPlugin
import org.gradle.StartParameter
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

        val buildMetricsExtension = project.extensions.create<BuildMetricsExtension>("buildMetrics").apply {
            branchName.convention(project.gitStateProvider().map { it.currentBranch.name })
            repoName.convention(project.repoName)
        }

        if (!project.pluginIsEnabled) {
            project.logger.lifecycle("Build metrics plugin is disabled")
            return
        }

        project.afterEvaluate {
            if (!buildMetricsExtension.buildType.isPresent || !buildMetricsExtension.environment.isPresent) {
                project.logger.warn(
                    """
                    Build metrics plugin configuration error. Plugin can't work.
                    Please configure buildType and environment at buildMetrics extension
                """.trimIndent()
                )
            } else if (
                buildMetricsExtension.writeModulesBuildTime.get() &&
                !buildMetricsExtension.modulesBuildTimeFile.isPresent
            ) {
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
                            buildType.set(buildMetricsExtension.buildType)
                            userName.set(buildMetricsExtension.userName)
                            environment.set(buildMetricsExtension.environment)
                            statsdConfig.set(project.statsdConfig)
                            graphiteConfig.set(project.graphiteConfig)
                            clickStreamConfig.set(project.clickStreamConfig)
                            sendCompileMetrics.set(buildMetricsExtension.sendCompileMetrics)
                            compileMetricsMinimumDuration.set(buildMetricsExtension.compileMetricsMinimumDuration)
                            sendSlowTaskMetrics.set(buildMetricsExtension.sendSlowTaskMetrics)
                            slowTaskMinimumDuration.set(buildMetricsExtension.slowTaskMinimumDuration)
                            sendBuildCacheMetrics.set(buildMetricsExtension.sendBuildCacheMetrics)
                            canTrackRemoteCache.set(BuildOperationsResultProvider.canTrackRemoteCache(project))
                            buildCacheObservableTasks.set(buildMetricsExtension.buildCacheObservableTasks)
                            writeModulesBuildTime.set(buildMetricsExtension.writeModulesBuildTime)
                            modulesBuildTimeFile.set(buildMetricsExtension.modulesBuildTimeFile)
                            sendJvmMetrics.set(buildMetricsExtension.sendJvmMetrics)
                            sendOsMetrics.set(buildMetricsExtension.sendOsMetrics)
                            sendBuildInitConfiguration.set(buildMetricsExtension.sendBuildInitConfiguration)
                            sendBuildTotal.set(buildMetricsExtension.sendBuildTotal)
                            sendAppBuildTime.set(buildMetricsExtension.sendAppBuildTime)
                            sendTestRunnerMetrics.set(buildMetricsExtension.sendTestRunnerMetrics)
                            branchName.set(buildMetricsExtension.branchName)
                            repoName.set(buildMetricsExtension.repoName)
                            requestedTasks.set(project.gradle.startParameter.taskNames)
                            bootstrapRequestedTaskNames.set(buildMetricsExtension.bootstrapRequestedTaskNames)
                            sendRequestedTasksMetrics.set(buildMetricsExtension.sendRequestedTasksMetrics)
                            invokedFromIde.set(isInvokedFromIde(project.gradle.startParameter))
                            executionHistoryFile.set(
                                project.layout.file(
                                    project.providers.provider {
                                        val repoKey = BuildExecutionHistory.storageKey(
                                            originUrl = BuildExecutionHistory.gitOriginUrlProvider(project).orNull,
                                            repoName = buildMetricsExtension.repoName.orNull,
                                        )
                                        project.gradle.gradleUserHomeDir
                                            .resolve("build-metrics/execution-history/$repoKey.properties")
                                    }
                                )
                            )
                            projectDir.set(project.layout.projectDirectory)
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
                        buildMetricsExtension,
                        GradleLoggerPlugin.getLoggerFactory(project)
                    )
                    registerNotCompatibleWithCCListeners(di, buildMetricsExtension)
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

private val Project.repoName: String
    get() {
        val key = getOptionalStringProperty(
            "avito.bitbucket.projectKey",
            default = "",
        )
        val name = getOptionalStringProperty(
            "avito.bitbucket.repositorySlug",
            default = "",
        )
        return "$key/$name"
    }

private fun isInvokedFromIde(startParameter: StartParameter): Boolean {
    val properties = startParameter.projectProperties
    val ideRunKey = "android.injected.invoked.from.ide"
    return properties.containsKey(ideRunKey) && properties[ideRunKey] == "true"
}
