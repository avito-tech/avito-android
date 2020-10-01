package com.avito.android.build_verdict

import com.avito.kotlin.dsl.isRoot
import com.avito.utils.logging.ciLogger
import com.google.gson.GsonBuilder
import org.gradle.api.Plugin
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.kotlin.dsl.create

internal data class TaskPath(val value: String)

class BuildVerdictPlugin : Plugin<ProjectInternal> {

    override fun apply(target: ProjectInternal) {
        require(target.isRoot()) {
            "build-verdict plugin must be applied to the root project"
        }
        val logs = mutableMapOf<TaskPath, StringBuilder>()
        val extension = target.extensions.create<BuildVerdictPluginExtension>("buildVerdict")
        target.gradle.addListener(TaskErrorOutputCaptureExecutionListener(logs))
        target.gradle.taskGraph.whenReady { graph ->
            target.gradle.buildFinished(
                BuildFailureListener(
                    graph = graph,
                    buildVerdictDir = extension.buildVerdictOutputDir,
                    logs = logs,
                    ciLogger = target.ciLogger,
                    gson = GsonBuilder()
                        .disableHtmlEscaping()
                        .setPrettyPrinting()
                        .create()
                )
            )
        }
    }
}
