package com.avito.ci.steps

import com.avito.ci.VerifyOutputsTask
import com.avito.impact.configuration.internalModule
import com.avito.kotlin.dsl.typedNamed
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.create
import com.avito.plugin.signedApkTaskProvider
import com.avito.plugin.signedBundleTaskProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.io.File

open class VerifyArtifactsStep(
    context: String,
    private val artifactsConfig: ArtifactsConfiguration,
    name: String
) : BuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        if (useImpactAnalysis && !project.internalModule.isModified()) return

        rootTask.configure { task ->
            artifactsConfig.outputs.forEach { (_, output) ->
                when (output) {
                    is Output.ApkOutput -> task.dependsOn(project.tasks.signedApkTaskProvider(output.variantName))
                    is Output.BundleOutput -> task.dependsOn(project.tasks.signedBundleTaskProvider(output.variantName))
                    is Output.ProguardMapping,
                    is Output.FileOutput -> {
                        // do nothing
                    }
                }
            }
        }

        val copyTask = project.tasks.register<CopyArtifactsTask>("${context}CopyArtifacts") {
            group = cdTaskGroup
            description = "Copies all defined release artifacts to outputs directory"

            sourceDir.set(project.rootDir)
            destinationDir.set(File("${project.rootProject.rootDir}/outputs"))
            entries.set(project.files(artifactsConfig.outputs.values.map { it.path }))

            val logger = GradleLoggerFactory.fromProject(project).create<VerifyArtifactsStep>()

            project.gradle.onBuildFailed {
                if (!didWork) {
                    // Copy artifacts that managed to be generated.
                    // Do not verify them because it is last resort to save anything.
                    logger.debug("Build failed. Trying to copy artifacts that managed to be generated.")
                    doAction()
                }
            }
        }

        val verifyOutputsTask = project.tasks.register<VerifyOutputsTask>(verifyTaskName(context)) {
            group = "cd"
            description = "Checks that all defined release artifacts are present"
            config.set(artifactsConfig)
            checkSignatures.set(artifactsConfig.failOnSignatureError)
            dependsOn(copyTask)
        }

        rootTask.configure { task ->
            task.finalizedBy(verifyOutputsTask)
        }
    }
}

internal fun verifyTaskName(context: String) = "${context}VerifyArtifacts"

internal fun TaskContainer.verifyTaskProvider(context: String) = typedNamed<VerifyOutputsTask>(verifyTaskName(context))

private fun Gradle.onBuildFailed(block: () -> Unit) {
    buildFinished { buildResult ->
        if (buildResult.failure != null && buildResult.action == ACTION_BUILD) {
            block()
        }
    }
}

private const val ACTION_BUILD = "Build"
