package com.avito.android.artifactory_backup

import com.avito.android.http.ArtifactoryClient
import com.avito.android.model.input.Deployment
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import okhttp3.HttpUrl.Companion.toHttpUrl

internal class ArtifactoryBackupAction(
    private val artifactoryClient: ArtifactoryClient,
    private val artifactsFactory: ArtifactsFactory<Deployment>,
    private val artifactsAdapter: ArtifactsAdapter,
    loggerFactory: LoggerFactory,
) {

    private val logger = loggerFactory.create<ArtifactoryBackupAction>()

    fun backup(
        artifactoryUploadPath: String,
        deployments: List<Deployment>,
    ): String {
        val destinationFolder = artifactoryUploadPath.toHttpUrl()

        val artifacts = mutableListOf<Artifact>()

        logger.info("Uploading artifacts: path=$artifactoryUploadPath, deployments=$deployments")

        deployments.forEach { deployment ->
            val url = destinationFolder.newBuilder().addEncodedPathSegment(deployment.file.name).build()
            val response = artifactoryClient.uploadFile(url, deployment.file)
            val artifact = artifactsFactory.create(deployment, url)

            if (response.isSuccessful) {
                artifacts.add(artifact)
                logger.info("Artifact uploaded successfully: $artifact")
            } else {
                logger.warn(
                    "Can't upload artifact: $artifact; " +
                        "code=${response.code}; " +
                        "body=${response.body?.string()}"
                )
            }
        }

        return artifactsAdapter.toJson(artifacts)
    }
}
