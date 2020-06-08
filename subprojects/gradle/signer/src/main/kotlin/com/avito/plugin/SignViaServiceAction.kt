package com.avito.plugin

import com.avito.utils.*
import com.avito.utils.logging.CILogger
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.funktionale.tries.Try
import java.io.File
import java.util.concurrent.TimeUnit

internal class SignViaServiceAction(
    private val serviceUrl: String,
    private val token: String,
    private val unsignedFile: File,
    private val signedFile: File,
    private val ciLogger: CILogger
) {

    private val apiPath = "/sign"

    fun sign(): Try<ExistingFile> {
        return Try {
            retry(
                retriesCount = 6,
                delaySeconds = 0,
                attemptFailedHandler = { attempt, throwable ->
                    ciLogger.critical("Attempt $attempt: failed to sign apk via service ${throwable.getStackTraceString()}")
                },
                actionFailedHandler = { throwable ->
                    val message = "Failed to sign apk via service: " + throwable.getStackTraceString()
                    ciLogger.critical(message)
                    throw IllegalStateException(message, throwable)
                }
            ) {
                val request = buildRequest()

                val response =
                    buildHttpClient()
                        .newCall(request)
                        .execute()

                response.process(outputFile = signedFile)
                signedFile.toExisting()
            }
        }
    }

    private fun buildHttpClient() = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .addInterceptor(
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    ciLogger.info(message)
                }
            }).setLevel(HttpLoggingInterceptor.Level.BASIC)
        )
        .build()

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
            .build()
    }

    private fun Response.process(outputFile: File) {
        if (this.isSuccessful) {
            outputFile.createOrClear()
            this.body
                ?.byteStream()
                ?.use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
        } else {
            val stringBody = this.body
                ?.string()
                ?: "Cannot read the response body"

            error("Sign service returned: ${this.code}, body: $stringBody")
        }
    }
}
