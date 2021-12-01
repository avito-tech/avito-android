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
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }
}
