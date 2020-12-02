package com.avito.android.elastic

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

internal class ElasticServiceFactory(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson = GsonBuilder().create()
) {

    fun createApiService(endpoint: String): ElasticService =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(endpoint)
            .build()
            .create()
}
