package com.avito.ci.steps.deploy

import com.avito.cd.AndroidArtifactType
import com.avito.cd.CdBuildConfig
import com.avito.ci.steps.ArtifactsConfiguration
import com.avito.ci.steps.Output
import com.avito.upload_to_googleplay.GooglePlayDeploy
import java.io.File
import java.util.Locale

internal class ToGooglePlayDeploysTransformer(
    private val artifacts: ArtifactsConfiguration
) {
    fun transform(deployment: CdBuildConfig.Deployment.GooglePlay): GooglePlayDeploy {
        return try {
            val outputs = artifacts.outputs.values
            val mappings = outputs.findOutput<Output.ProguardMapping> { artifact ->
                artifact.variant == deployment.buildVariant
            }
            when (deployment.artifactType) {
                AndroidArtifactType.APK -> {
                    val apk = outputs.findOutput<Output.ApkOutput> { output ->
                        output.variant == deployment.buildVariant
                    }

                    GooglePlayDeploy(
                        binaryType = GooglePlayDeploy.BinaryType.APK,
                        track = deployment.track.name.lowercase(Locale.getDefault()),
                        applicationId = apk.packageName,
                        binary = File(apk.path),
                        mapping = File(mappings.path)
                    )
                }
                AndroidArtifactType.BUNDLE -> {
                    val bundle = outputs.findOutput<Output.BundleOutput> { output ->
                        output.variant == deployment.buildVariant
                    }

                    GooglePlayDeploy(
                        binaryType = GooglePlayDeploy.BinaryType.BUNDLE,
                        track = deployment.track.name.lowercase(Locale.getDefault()),
                        applicationId = bundle.packageName,
                        binary = File(bundle.path),
                        mapping = File(mappings.path)
                    )
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException("Can't configure deployment=$deployment", e)
        }
    }

    private inline fun <reified O : Output> Iterable<Output>.findOutput(
        noinline predicate: (O) -> Boolean
    ): O {
        val outputs: Sequence<O> = asSequence()
            .filterIsInstance<O>()
            .filter(predicate)

        outputs.ifEmpty {
            throw IllegalStateException("Cant' find output ${O::class.java}. Artifacts that didn't fit $this")
        }

        return outputs.singleOrNull()
            ?: throw IllegalStateException(
                "Too many outputs of type ${O::class.java} fit. Must be single from $outputs"
            )
    }
}
