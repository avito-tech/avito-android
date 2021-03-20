package com.avito.plugin

import com.avito.android.Result
import com.avito.http.HttpLogger
import com.avito.http.RetryInterceptor
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.create<QAppsUploadAction>()

    private val apiClient by lazy {
        Retrofit.Builder()
            .baseUrl(host)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(httpClient())
            .validateEagerly(true)
            .build()
            .create(QAppsUploadApi::class.java)
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

    private fun httpClient() = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .addInterceptor(
            RetryInterceptor(
                retries = 3,
                allowedMethods = listOf("POST", "GET"),
                logger = logger
            )
        )
        .addInterceptor(
            HttpLoggingInterceptor(HttpLogger(logger)).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()
}
