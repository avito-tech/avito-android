package com.avito.android.tech_budget.internal.di

import com.avito.logger.LoggerFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

internal class RetrofitProvider(
    private val baseUrl: String,
    private val moshiProvider: MoshiProvider,
    private val loggerFactory: LoggerFactory
) {

    fun provide(): Retrofit {
        val moshiConverterFactory =
            MoshiConverterFactory.create(
                moshiProvider.provide()
            ).failOnUnknown()

        val logger = loggerFactory.create("OkHttp")
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor(logger::info).setLevel(HttpLoggingInterceptor.Level.BASIC)
            )
            .build()

        return Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .build()
    }
}
