package com.avito.ci

import com.avito.android.androidSdk
import com.avito.ci.steps.ArtifactsConfiguration
import com.avito.ci.steps.Output
import com.avito.plugin.SignVerifier
import com.avito.utils.exhaustive
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Verifies that all declared release artifacts are built and copied to outputs folder
 * e.g. checks that all artifacts produces and placed correctly
 */
abstract class VerifyOutputsTask : DefaultTask() {

    /**
     * hardcoded here, because it's a contract check
     * it's possible to get this path, but it's wrong
     */
    @Suppress("ANNOTATION_TARGETS_NON_EXISTENT_ACCESSOR")
    @get:InputDirectory
    private val outputsDir = File("${project.rootProject.rootDir.canonicalPath}/outputs")

    @Input
    lateinit var config: ArtifactsConfiguration

    @Input
    var checkSignatures: Boolean = true

    @TaskAction
    fun doWork() {
        val signVerifier = SignVerifier.Impl(project.androidSdk)
        val outputsVerifier = OutputsVerifier(project.androidSdk, signVerifier, outputsDir)

        config.outputs.forEach { (_: String, output: Output) ->
            // Check file that have already been copied to outputs folder
            val originalArtifact = File(output.path)
                .relativeTo(project.rootProject.projectDir)

            val artifactPath = File(outputsDir, originalArtifact.path)
            ciLogger.info("Verify artifact ${artifactPath.path}")

            when (output) {
                is Output.ProguardMapping,
                is Output.FileOutput -> outputsVerifier.requireFile(artifactPath)
                is Output.ApkOutput -> outputsVerifier.requireFile(artifactPath) {
                    if (checkSignatures && output.signature != null) {
                        outputsVerifier.checkApkSignature(it, output.signature!!)
                    }
                    outputsVerifier.checkPackageName(artifactPath, output.packageName)
                }
                is Output.BundleOutput -> outputsVerifier.requireFile(artifactPath) {
                    if (checkSignatures && output.signature != null) {
                        outputsVerifier.checkBundleSignature(it, output.signature!!)
                    }
                }
            }.exhaustive()
        }

        if (outputsVerifier.errors.isNotEmpty()) {
            error(outputsVerifier.errors.joinToString(prefix = "CI contract violation!\n", separator = "\n"))
        } else {
            ciLogger.info("All artifacts checked")
        }
    }
}
