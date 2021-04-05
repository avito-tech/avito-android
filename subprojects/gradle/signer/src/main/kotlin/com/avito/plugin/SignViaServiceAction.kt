package com.avito.plugin

import com.avito.android.Result
import com.avito.http.internal.RequestMetadata
import com.avito.utils.ExistingFile
import com.avito.utils.createOrClear
import com.avito.utils.toExisting
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File

internal class SignViaServiceAction(
    private val serviceUrl: String,
    private val httpClient: OkHttpClient,
    private val token: String,
    private val unsignedFile: File,
    private val signedFile: File,
) {

    private val apiPath = "/sign"

    fun sign(): Result<ExistingFile> = Result.tryCatch {
        val request = buildRequest()

        val response = httpClient
            .newCall(request)
            .execute()

        writeResponse(response, outputFile = signedFile)

        signedFile.toExisting()
    }

    private fun buildRequest(): Request {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(
                MultipartBody.Part.createFormData("token", token)
            )
            .addPart(
                MultipartBody.Part.createFormData(
                    "files",
                    unsignedFile.name,
                    unsignedFile.asRequestBody(null)
                )
            )
            .build()

        val hostWithoutSlash = serviceUrl.removeSuffix("/")

        return Request.Builder()
            .url(hostWithoutSlash + apiPath)
            .post(body)
            .tag(
                RequestMetadata::class.java,
                RequestMetadata("signer", "sign")
            )
            .build()
    }

    private fun writeResponse(response: Response, outputFile: File) {
        if (response.isSuccessful) {
            outputFile.createOrClear()
            response.body
                ?.byteStream()
                ?.use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
        } else {
            val stringBody = response.body
                ?.string()
                ?: "Cannot read the response body"

            error("Failed to sign APK via service: code ${response.code}, body: $stringBody")
        }
    }
}
