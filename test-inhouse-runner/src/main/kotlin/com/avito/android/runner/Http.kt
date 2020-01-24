package com.avito.android.runner

import android.annotation.SuppressLint
import android.util.Log
import com.avito.http.RetryInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@SuppressLint("LogNotTimber")
internal fun createReportHttpClient(): OkHttpClient {
    val retryInterceptor = RetryInterceptor(allowedMethods = listOf("GET", "POST"),
        logger = { message, error -> Log.v(TAG, message, error) })
    val httpLoggingInterceptor = HttpLoggingInterceptor { Log.v(TAG, it) }
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
private const val TAG = "ReportViewerHttp"
