package com.avito.android.elastic

import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

internal class ElasticHttpServiceFactory(
    private val endpoint: HttpUrl,
    private val okHttpClientBuilder: OkHttpClient.Builder,
    private val gson: Gson
) : ElasticServiceFactory {

    override fun provide(): ElasticService =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClientBuilder.build())
            .baseUrl(endpoint)
            .build()
            .create()
}
