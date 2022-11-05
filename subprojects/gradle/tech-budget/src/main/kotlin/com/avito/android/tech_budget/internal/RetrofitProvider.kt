package com.avito.android.tech_budget.internal

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class RetrofitProvider(
    private val baseUrl: String,
) {

    fun provide(): Retrofit {
        val moshiConverterFactory =
            MoshiConverterFactory.create(
                Moshi.Builder().build()
            ).failOnUnknown()

        return Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(OkHttpClient.Builder().build())
            .baseUrl(baseUrl)
            .build()
    }
}
