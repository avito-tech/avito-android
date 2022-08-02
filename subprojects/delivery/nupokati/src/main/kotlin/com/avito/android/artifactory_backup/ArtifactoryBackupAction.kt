package com.avito.android.artifactory_backup

import com.avito.android.http.ArtifactoryClient
import com.avito.android.model.input.Deployment
import com.avito.android.model.output.Artifact
import com.avito.android.model.output.ArtifactsAdapter
import com.avito.android.model.output.ArtifactsFactory
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.logging.Logger

internal class ArtifactoryBackupAction(
    private val artifactoryClient: ArtifactoryClient,
    private val artifactsFactory: ArtifactsFactory<Deployment>,
    private val artifactsAdapter: ArtifactsAdapter,
    private val logger: Logger,
) {

    fun backup(
        artifactoryUploadPath: String,
        deployments: List<Deployment>,
    ): String {
        val destinationFolder = artifactoryUploadPath.toHttpUrl()

        val artifacts = mutableListOf<Artifact>()

        logger.lifecycle("Uploading artifacts: path=$artifactoryUploadPath, deployments=$deployments")

        deployments.forEach { deployment ->
            val url = destinationFolder.newBuilder().addEncodedPathSegment(deployment.file.name).build()
            val response = artifactoryClient.uploadFile(url, deployment.file)
            val artifact = artifactsFactory.create(deployment, url)

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
