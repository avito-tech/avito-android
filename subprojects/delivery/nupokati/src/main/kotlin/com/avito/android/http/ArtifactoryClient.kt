package com.avito.android.http

import com.avito.http.internal.RequestMetadata
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File

internal class ArtifactoryClient(
    private val httpClient: OkHttpClient,
    private val serviceName: String = "nupokati-artifactory"
) {

    private val applicationJson = "application/json".toMediaType()

    fun uploadFile(url: HttpUrl, file: File): Response {
        return upload(url, file.asRequestBody(file.guessContentType()))
    }

    fun uploadJson(url: String, fileContent: String): Response {
        return upload(url.toHttpUrl(), fileContent.toRequestBody(applicationJson))
    }

    private fun upload(url: HttpUrl, requestBody: RequestBody): Response {
        val request = Request.Builder()
            .put(requestBody)
            .url(url)
            .tag(
                RequestMetadata::class.java,
                RequestMetadata(serviceName = serviceName, methodName = "upload")
            )
            .build()

        return httpClient.newCall(request).execute()
    }

    private fun File.guessContentType(): MediaType? {
        return when (extension) {
            "json" -> applicationJson
            else -> null
        }
    }
}
