package com.avito.plugin

import com.avito.android.Result
import com.avito.http.HttpLogger
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.utils.ExistingFile
import com.avito.utils.createOrClear
import com.avito.utils.toExisting
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

internal class SignViaServiceAction(
    private val serviceUrl: String,
    private val token: String,
    private val unsignedFile: File,
    private val signedFile: File,
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.create<SignViaServiceAction>()

    private val apiPath = "/sign"

    fun sign(): Result<ExistingFile> {
        return Result.tryCatch {
            retry(
                retriesCount = 6,
                delaySeconds = 0,
                attemptFailedHandler = { attempt, throwable ->
                    logger.critical("Attempt $attempt: failed to sign apk via service", throwable)
                },
                actionFailedHandler = { throwable ->
                    val message = "Failed to sign apk via service"
                    logger.critical(message, throwable)
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
        .addInterceptor(HttpLoggingInterceptor(HttpLogger(logger)).setLevel(HttpLoggingInterceptor.Level.BASIC))
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

    private fun <T> retry(
        retriesCount: Int,
        delaySeconds: Long = 1,
        attemptFailedHandler: (attempt: Int, throwable: Throwable) -> Unit = { _, _ -> },
        actionFailedHandler: (throwable: Throwable) -> Unit = { },
        block: (attempt: Int) -> T
    ): T {
        var throwable: Throwable? = null

        (1..retriesCount).forEach { attempt ->
            try {
                return block(attempt)
            } catch (e: Throwable) {
                throwable = e
                attemptFailedHandler(attempt, e)
                TimeUnit.SECONDS.sleep(delaySeconds)
            }
        }

        actionFailedHandler(throwable!!)

        throw throwable!!
    }
}
