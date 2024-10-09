package com.avito.android.elastic

import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

internal class ElasticHttpServiceFactory(
    private val endpoint: HttpUrl,
    private val okHttpClientBuilder: OkHttpClient.Builder,
    private val gson: GsonBuilder,
) {

    fun provide(): ElasticApi =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson.create()))
            .client(okHttpClientBuilder.build())
            .baseUrl(endpoint)
            .build()
            .create()
}
