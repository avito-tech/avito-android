package com.avito.ci

import com.avito.android.androidSdk
import com.avito.ci.internal.signer.SignVerifier
import com.avito.ci.internal.signer.SignVerifierImpl
import com.avito.ci.steps.ArtifactsConfiguration
import com.avito.ci.steps.Output
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

/**
 * Verifies that all declared release artifacts are built and copied to outputs folder
 * e.g. checks that all artifacts produces and placed correctly
 */
public abstract class VerifyOutputsTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    /**
     * hardcoded here, because it's a contract check
     * it's possible to get this path, but it's wrong
     */
    @Suppress("ANNOTATION_TARGETS_NON_EXISTENT_ACCESSOR")
    @get:InputDirectory
    private val outputsDir = File("${project.rootProject.rootDir.canonicalPath}/outputs")

    @Input
    public val config: Property<ArtifactsConfiguration> = objects.property()

    @Input
    public val checkSignatures: Property<Boolean> = objects.property()

    @TaskAction
    public fun doWork() {
        val signVerifier: SignVerifier = SignVerifierImpl(project.androidSdk)
        val outputsVerifier = OutputsVerifier(
            androidSdk = project.androidSdk,
            signVerifier = signVerifier,
            outputsDir = outputsDir
        )

        config.get().outputs.forEach { (_: String, output: Output) ->
            // Check file that have already been copied to outputs folder
            val originalArtifact = File(output.path)
                .relativeTo(project.rootProject.projectDir)

            val artifactPath = File(outputsDir, originalArtifact.path)
            logger.info("Verify artifact ${artifactPath.path}")

            when (output) {
                is Output.ProguardMapping,
                is Output.FileOutput -> outputsVerifier.requireFile(artifactPath)
                is Output.ApkOutput -> outputsVerifier.requireFile(artifactPath) {
                    if (checkSignatures.get() && output.signature != null) {
                        outputsVerifier.checkApkSignature(it, output.signature!!)
                    }
                    outputsVerifier.checkPackageName(artifactPath, output.packageName)
                }
                is Output.BundleOutput -> outputsVerifier.requireFile(artifactPath) {
                    if (checkSignatures.get() && output.signature != null) {
                        outputsVerifier.checkBundleSignature(it, output.signature!!)
                    }
                }
            }
        }

        if (outputsVerifier.errors.isNotEmpty()) {
            error(outputsVerifier.errors.joinToString(prefix = "CI contract violation!\n", separator = "\n"))
        } else {
            logger.info("All artifacts checked")
        }
    }
}
