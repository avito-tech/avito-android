package com.avito.android.artifactory_backup

import com.avito.android.model.input.DeploymentV3
import okhttp3.HttpUrl
import java.io.File

internal class ArtifactsV3Factory : ArtifactsFactory<DeploymentV3> {

    override fun create(deployment: DeploymentV3, url: HttpUrl): Artifact {
        val file = deployment.file
        return when (deployment) {
            is DeploymentV3.AppBinary -> ArtifactV3.AppBinary(
                store = deployment.store,
                fileType = getAndroidAppFileType(deployment.file),
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
                fileType = getAndroidAppFileType(deployment.file),
                name = file.name,
                uri = url.toString(),
                store = deployment.store,
                buildConfiguration = deployment.buildConfiguration,
            )
        }
    }

    private fun getAndroidAppFileType(file: File): String {
        return when (file.extension) {
            "aab" -> "bundle"
            "apk" -> "apk"
            else -> throw IllegalArgumentException("Unsupported android file app extension: ${file.extension}")
        }
    }
}
