package com.avito.android.build_verdict

import com.avito.kotlin.dsl.isRoot
import com.avito.utils.logging.ciLogger
import com.google.gson.GsonBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.kotlin.dsl.create
import org.gradle.util.Path

internal data class TaskPath(val value: String)

@Suppress("UnstableApiUsage")
class BuildVerdictPlugin : Plugin<ProjectInternal> {

    override fun apply(project: ProjectInternal) {
        require(project.isRoot()) {
            "build-verdict plugin must be applied to the root project"
        }

        if (project.pluginIsEnabled) {
            val logs = mutableMapOf<Path, StringBuilder>()
            val extension = project.extensions.create<BuildVerdictPluginExtension>("buildVerdict")
            project.gradle.addListener(TaskErrorOutputCaptureExecutionListener(logs, project.ciLogger))
            project.gradle.taskGraph.whenReady { graph ->
                val outputDir = extension.outputDir.get().asFile
                project.gradle.buildFinished(
                    BuildFailureListener(
                        graph = graph,
                        logs = logs,
                        writer = CompositeBuildVerdictWriter(
                            writers = listOf(
                                RawBuildVerdictWriter(
                                    buildVerdictDir = outputDir,
                                    logger = project.ciLogger,
                                    gson = GsonBuilder()
                                        .disableHtmlEscaping()
                                        .setPrettyPrinting()
                                        .create()
                                ),
                                PlainTextBuildVerdictWriter(
                                    buildVerdictDir = outputDir,
                                    logger = project.ciLogger
                                )
                            )
                        )
                    )
                )
            }
        }
    }

    internal val Project.pluginIsEnabled: Boolean
        get() = providers
            .gradleProperty(enabledProp)
            .forUseAtConfigurationTime()
            .map { it.toBoolean() }
            .getOrElse(true)

    internal companion object {
        val enabledProp = "avito.build-verdict.enabled"
    }
}
