package com.avito.cd

import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal object Providers {

    private val clientBuilder: OkHttpClient.Builder by lazy {
        OkHttpClient.Builder()
    }

    internal fun client(
        user: String,
        password: String,
        logger: HttpLoggingInterceptor.Logger = HttpLoggingInterceptor.Logger.DEFAULT
    ): OkHttpClient {
        return clientBuilder
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request()
                        .newBuilder()
                        .addHeader("Authorization", Credentials.basic(user, password))
                        .build()
                )
            }
            .addInterceptor(
                HttpLoggingInterceptor(logger).apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }
}
