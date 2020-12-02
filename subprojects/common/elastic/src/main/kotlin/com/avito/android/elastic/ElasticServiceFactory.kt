package com.avito.android.elastic

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

internal class ElasticServiceFactory(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson = GsonBuilder().create()
) {

    fun createApiService(endpoints: List<String>): ElasticService =
        when (endpoints.size) {
            0 -> error("Elastic service endpoints has not been provided")
            1 -> createApiService(endpoints[0])
            else -> RoundRobinService(this, endpoints)
        }

    private fun createApiService(endpoint: String): ElasticService =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(endpoint)
            .build()
            .create()

    private class RoundRobinService(factory: ElasticServiceFactory, endpoints: List<String>) : ElasticService {

        private val roundRobin =
            RoundRobinIterable(endpoints.map { endpoint -> lazy { factory.createApiService(endpoint) } })

        override fun log(indexPattern: String, date: String, logEvent: ElasticLogEventRequest): Call<ResponseBody> =
            roundRobin.next().value.log(
                indexPattern = indexPattern,
                date = date,
                logEvent = logEvent
            )
    }
}
