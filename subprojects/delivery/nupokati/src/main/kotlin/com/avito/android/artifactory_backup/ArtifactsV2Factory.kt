package com.avito.android.artifactory_backup

import com.avito.android.model.input.DeploymentV2
import okhttp3.HttpUrl
import java.io.File

internal class ArtifactsV2Factory : ArtifactsFactory<DeploymentV2> {

    override fun create(deployment: DeploymentV2, url: HttpUrl): Artifact {
        return if (deployment.file.isAndroidBinary()) {
            ArtifactV2.AndroidBinary(
                type = deployment.file.appBinaryFileType(),
                name = deployment.file.name,
                uri = url.toString(),
                buildVariant = deployment.buildVariant
            )
        } else {
            ArtifactV2.FileArtifact(
                type = deployment.file.extension,
                name = deployment.file.name,
                uri = url.toString()
            )
        }
    }

    private fun File.appBinaryFileType(): String = when (extension) {
        "aab" -> "bundle"
        "apk" -> "apk"
        else -> throw IllegalArgumentException("Unknown file type with extension: '$extension', full path: $path")
    }

    private fun File.isAndroidBinary(): Boolean = extension == "aab" || extension == "apk"
}
