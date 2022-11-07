package com.avito.emcee.client.internal

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File

internal class ArtifactoryFileUploader(
    private val httpClient: OkHttpClient,
    private val artifactorySettings: ArtifactorySettings,
    private val bucketName: String,
) : FileUploader {

    private val applicationApk = "application/vnd.android.package-archive".toMediaType()

    override suspend fun upload(file: File): HttpUrl {
        val url: HttpUrl = artifactorySettings.baseUrl.toHttpUrl().newBuilder()
            .addEncodedPathSegment(artifactorySettings.repository)
            .addEncodedPathSegment(artifactorySettings.folder)
            .addEncodedPathSegment(bucketName)
            .addEncodedPathSegment(file.name)
            .build()

        val response = uploadApk(url, file)
        require(response.isSuccessful) {
            "Failed to upload $file. $response"
        }

        return url
    }

    private fun uploadApk(url: HttpUrl, file: File): Response {
        return upload(url, file.asRequestBody(file.guessContentType()))
    }

    private fun upload(url: HttpUrl, requestBody: RequestBody): Response {
        val request = Request.Builder()
            .put(requestBody)
            .url(url)
            .build()

        return httpClient.newCall(request).execute()
    }

    private fun File.guessContentType(): MediaType? {
        return when (extension) {
            "apk" -> applicationApk
            else -> null
        }
    }
}
