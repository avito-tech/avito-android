package com.avito.upload_to_googleplay

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease
import org.slf4j.Logger
import java.io.File
import java.util.concurrent.TimeUnit

internal class GooglePlayDeployerImpl(
    jsonKey: File,
    private val logger: Logger
) : GooglePlayDeployer {

    private val publisher by lazy {
        createAndroidPublisher(jsonKey)
    }

    private val edits by lazy {
        publisher.edits()
    }

    override fun deploy(deploys: List<GooglePlayDeploy>) {
        val editIdWithDeploy = deploys
            .map { deploy ->
                val editId = insertEdit(deploy.applicationId)
                val versionCode = uploadBinary(deploy, editId)
                setTrack(deploy, editId, versionCode)
                validate(deploy, editId)
                editId to deploy
            }
        commitAll(editIdWithDeploy)
        logger.debug("SUCCESS: Deploy $deploys")
    }

    private fun insertEdit(applicationId: String): String {
        return edits.insert(applicationId, null).execute().id
    }

    private fun uploadBinary(deploy: GooglePlayDeploy, editId: String): Int {
        return when (deploy.binaryType) {
            GooglePlayDeploy.BinaryType.APK -> {
                val versionCode = uploadApk(deploy, editId)
                uploadProguard(deploy, editId, versionCode)
                versionCode
            }
            GooglePlayDeploy.BinaryType.BUNDLE ->
                uploadBundle(deploy, editId)
        }
    }

    private fun uploadBundle(
        deploy: GooglePlayDeploy,
        editId: String
    ): Int {
        logger.info("Uploading bundle ${deploy.binary.absolutePath}")
        return edits.bundles()
            .upload(
                deploy.applicationId,
                editId,
                FileContent(
                    MIME_TYPE_STREAM,
                    deploy.binary
                )
            )
            .trackUploadProgress("bundle")
            .execute().versionCode
    }

    private fun uploadApk(
        deploy: GooglePlayDeploy,
        editId: String
    ): Int {
        logger.info("Uploading apk ${deploy.binary.absolutePath}")
        return edits.apks()
            .upload(
                deploy.applicationId,
                editId,
                FileContent(
                    "application/vnd.android.package-archive",
                    deploy.binary
                )
            )
            .trackUploadProgress("apk")
            .execute().versionCode
    }

    private fun uploadProguard(
        deploy: GooglePlayDeploy,
        editId: String,
        versionCode: Int
    ) {
        logger.info("Upload proguard mapping")
        edits.deobfuscationfiles()
            .upload(
                deploy.applicationId,
                editId,
                versionCode,
                "proguard",
                FileContent(
                    MIME_TYPE_STREAM,
                    deploy.mapping
                )
            )
            .trackUploadProgress("proguard mapping")
            .execute()
    }

    private fun setTrack(
        deploy: GooglePlayDeploy,
        editId: String,
        versionCode: Int
    ) {
        logger.info("Updating track")
        val track = edits
            .tracks()
            .update(
                deploy.applicationId,
                editId,
                deploy.track,
                Track().also { track ->
                    track.track = deploy.track
                    track.releases = listOf(
                        TrackRelease()
                            .also { release ->
                                release.versionCodes = listOf(versionCode.toLong())
                                release.status = "completed"
                            }
                    )
                }
            ).execute()
        logger.info("Updated track is $track")
    }

    private fun validate(deploy: GooglePlayDeploy, editId: String) {
        try {
            logger.info("Validating edit of $deploy")
            edits.validate(deploy.applicationId, editId)
        } catch (e: Exception) {
            throw IllegalStateException("Fail edit validation of $deploy", e)
        }
    }

    private fun commitAll(applicationIdAndEditId: List<Pair<String, GooglePlayDeploy>>) {
        logger.info("Start committing")
        applicationIdAndEditId.forEach { (editId, deploy) ->
            try {
                edits.commit(deploy.applicationId, editId).execute()
            } catch (e: Exception) {
                throw IllegalStateException("Fail commit edit of $deploy", e)
            }
        }
    }

    companion object {

        const val MIME_TYPE_STREAM = "application/octet-stream"

        private val timeout = TimeUnit.MINUTES.toMillis(5).toInt()

        private fun createAndroidPublisher(jsonKey: File): AndroidPublisher {
            val transport = GoogleNetHttpTransport.newTrustedTransport()
            val factory = JacksonFactory.getDefaultInstance()
            val credential = GoogleCredential.fromStream(
                jsonKey.inputStream(),
                transport,
                factory
            ).createScoped(listOf(AndroidPublisherScopes.ANDROIDPUBLISHER))

            return AndroidPublisher.Builder(
                transport,
                factory
            ) { request ->
                credential.initialize(
                    request
                        .setConnectTimeout(timeout)
                        .setReadTimeout(timeout)
                )
            }.run {
                applicationName = "avito-google-play-publisher"
                build()
            }
        }
    }
}
