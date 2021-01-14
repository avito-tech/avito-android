package com.avito.android.elastic

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.net.URL

internal class ElasticServiceFactory(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson = GsonBuilder().create()
) {
    private val createApiService = { endpoint: URL ->
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(endpoint)
            .build()
            .create<ElasticService>()
    }

    fun createApiService(endpoints: List<URL>): ElasticService =
        when (endpoints.size) {
            0 -> error("Elastic service endpoints has not been provided")
            1 -> createApiService(endpoints[0])
            else -> RoundRobinService(createApiService, endpoints)
        }
}
