package com.avito.android.runner

import android.annotation.SuppressLint
import android.util.Log
import com.avito.http.RetryInterceptor
import com.avito.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@SuppressLint("LogNotTimber")
internal fun createReportHttpClient(): OkHttpClient {
    val retryInterceptor = RetryInterceptor(allowedMethods = listOf("GET", "POST"),
        logger = object : Logger {
            override fun debug(msg: String) {
                Log.v("ReportViewerHttp", msg)
            }

            override fun exception(msg: String, error: Throwable) {
                Log.v("ReportViewerHttp", msg, error)
            }

            override fun critical(msg: String, error: Throwable) {
                Log.v("ReportViewerHttp", msg, error)
            }
        })
    val httpLoggingInterceptor = HttpLoggingInterceptor { Log.v("ReportViewerHttp", it) }
        .apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

    return OkHttpClient.Builder()
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(retryInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .build()
}

private const val TIMEOUT_SECONDS = 30L
