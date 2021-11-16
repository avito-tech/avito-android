package com.avito.plugin

import com.avito.android.Result
import com.avito.http.HttpClientProvider
import com.avito.http.RetryInterceptor
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.File
import java.util.concurrent.TimeUnit

internal class QAppsUploadAction(
    private val apk: File,
    private val comment: String,
    private val host: String,
    private val branch: String,
    private val versionName: String,
    private val versionCode: String,
    private val packageName: String,
    private val releaseChain: Boolean,
    private val httpClientProvider: HttpClientProvider,
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.create<QAppsUploadAction>()

    private val apiClient by lazy {
        Retrofit.Builder()
            .baseUrl(host)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(
                httpClientProvider.provide()
                    .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                    .addInterceptor(
                        RetryInterceptor(
                            retries = 3,
                            allowedMethods = listOf("GET", "POST")
                        )
                    )
                    .build()
            )
            .validateEagerly(true)
            .build()
            .create<QAppsUploadApi>()
    }

    fun upload(): Result<Unit> = Result.tryCatch {
        val response = uploadRequest().execute()
        if (!response.isSuccessful) {
            val error = response.errorBody()?.string() ?: "unknown error"
            error("Can't upload apk to qapps: $error")
        }
    }

    private fun uploadRequest(): Call<Void> {
        logger.info(
            "qapps upload: " +
                "apk=${apk.path}, " +
                "branch=$branch, " +
                "version_name=$versionName, " +
                "version_code=$versionCode, " +
                "package_name=$packageName, " +
                "release_chain=$releaseChain, " +
                "comment=$comment"
        )
        return apiClient.upload(
            comment = MultipartBody.Part.createFormData("comment", comment),
            branch = MultipartBody.Part.createFormData("branch", branch),
            version_name = MultipartBody.Part.createFormData("version_name", versionName),
            version_code = MultipartBody.Part.createFormData("version_code", versionCode),
            package_name = MultipartBody.Part.createFormData("package_name", packageName),
            release_chain = MultipartBody.Part.createFormData("release_chain", releaseChain.toString()),
            apk = MultipartBody.Part.createFormData(
                "app",
                apk.name,
                apk.asRequestBody(null)
            )
        )
    }
}

private const val TIMEOUT_SEC = 60L
