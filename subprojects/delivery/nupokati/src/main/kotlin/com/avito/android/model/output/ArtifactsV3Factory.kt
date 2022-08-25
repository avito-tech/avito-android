package com.avito.android.model.output

import com.avito.android.model.input.DeploymentV3
import okhttp3.HttpUrl

internal class ArtifactsV3Factory : ArtifactsFactory<DeploymentV3> {

    override fun create(deployment: DeploymentV3, url: HttpUrl): Artifact {
        val file = deployment.file
        return when (deployment) {
            is DeploymentV3.AppBinary -> ArtifactV3.AppBinary(
                store = deployment.store,
                fileType = when (deployment.file.extension) {
                    "aab" -> "bundle"
                    "apk" -> "apk"
                    else -> throw IllegalArgumentException("Unsupported extension: ${deployment.file.extension}")
                },
                name = file.name,
                uri = url.toString(),
                buildConfiguration = deployment.buildConfiguration,
            )
            is DeploymentV3.Artifact -> ArtifactV3.FileArtifact(
                fileType = deployment.file.extension,
                name = file.name,
                uri = url.toString(),
                kind = deployment.kind,
            )
            is DeploymentV3.QApps -> ArtifactV3.QApps(
                fileType = file.extension,
                name = file.name,
                uri = url.toString(),
                isRelease = deployment.isRelease,
            )
        }
    }
}
