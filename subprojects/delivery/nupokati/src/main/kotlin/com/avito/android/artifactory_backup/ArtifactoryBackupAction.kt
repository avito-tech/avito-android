package com.avito.android.artifactory_backup

import com.avito.android.http.ArtifactoryClient
import com.avito.android.model.CdBuildResult
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.logging.Logger
import java.io.File

internal class ArtifactoryBackupAction(
    private val artifactoryClient: ArtifactoryClient,
    private val artifactsAdapter: CdBuildResultArtifactsAdapter,
    private val logger: Logger,
) {

    fun backup(artifactoryUploadPath: String, buildVariant: String, files: Set<File>): String {
        val destinationFolder = artifactoryUploadPath.toHttpUrl()

        val artifacts = mutableListOf<CdBuildResult.Artifact>()

        logger.lifecycle("Uploading artifacts: path=$artifactoryUploadPath, buildVariant=$buildVariant, files=$files")

        files.forEach { file ->
            val url = destinationFolder.newBuilder().addEncodedPathSegment(file.name).build()
            val response = artifactoryClient.uploadFile(url, file)
            val artifact = artifactsAdapter.create(file, url, buildVariant)

            if (response.isSuccessful) {
                artifacts.add(artifact)
                logger.lifecycle("Artifact uploaded successfully: $artifact")
            } else {
                logger.error(
                    "Can't upload artifact: $artifact; " +
                        "code=${response.code}; " +
                        "body=${response.body?.string()}"
                )
            }
        }

        return artifactsAdapter.toJson(artifacts)
    }
}
