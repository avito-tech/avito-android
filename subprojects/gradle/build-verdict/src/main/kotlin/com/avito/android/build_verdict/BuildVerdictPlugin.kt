package com.avito.android.build_verdict

import com.avito.android.build_verdict.internal.BuildVerdictPluginServices
import com.avito.kotlin.dsl.isRoot
import com.avito.utils.logging.ciLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.logging.LoggingManagerInternal
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.support.serviceOf

@Suppress("UnstableApiUsage")
class BuildVerdictPlugin : Plugin<ProjectInternal> {

    internal val Project.pluginIsEnabled: Boolean
        get() = providers
            .gradleProperty(enabledProp)
            .forUseAtConfigurationTime()
            .map { it.toBoolean() }
            .getOrElse(true)

    override fun apply(project: ProjectInternal) {
        require(project.isRoot()) {
            "build-verdict plugin must be applied to the root project"
        }

        if (project.pluginIsEnabled) {
            val extension = project.extensions.create<BuildVerdictPluginExtension>("buildVerdict")
            val services = BuildVerdictPluginServices(extension, project.ciLogger)
            project.gradle.addListener(services.gradleTaskExecutionListener())
            project.gradle.addLogEventListener(services.gradleLogEventListener())
            val configurationListener = services.gradleConfigurationListener()
            project.gradle.addBuildListener(configurationListener)
            project.gradle.taskGraph.whenReady { graph ->
                project.gradle.removeListener(configurationListener)
                project.gradle.addBuildListener(
                    services.gradleBuildFinishedListener(
                        graph
                    )
                )
            }
        }
    }

    private fun GradleInternal.addLogEventListener(
        gradleLogEventListener: OutputEventListener
    ) {
        val loggingManager = serviceOf<LoggingManagerInternal>()
        loggingManager.addOutputEventListener(gradleLogEventListener)
        buildFinished { _ ->
            loggingManager.removeOutputEventListener(gradleLogEventListener)
        }
    }

    internal companion object {
        const val enabledProp = "avito.build-verdict.enabled"
    }
}
