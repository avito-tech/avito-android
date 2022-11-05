package com.avito.android.tech_budget.internal.di

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class RetrofitProvider(
    private val baseUrl: String,
    private val moshiProvider: MoshiProvider = MoshiProvider()
) {

    fun provide(): Retrofit {
        val moshiConverterFactory =
            MoshiConverterFactory.create(
                moshiProvider.provide()
            ).failOnUnknown()

        return Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(OkHttpClient.Builder().build())
            .baseUrl(baseUrl)
            .build()
    }
}
