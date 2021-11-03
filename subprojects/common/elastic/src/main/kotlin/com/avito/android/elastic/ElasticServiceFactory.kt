package com.avito.android.elastic

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal interface ElasticServiceFactory {

    fun provide(): ElasticService

    companion object {

        private const val defaultTimeoutSec = 10L

        private val gson: Gson by lazy { GsonBuilder().create() }

        private val okHttpClientBuilder by lazy {
            OkHttpClient.Builder()
                .connectTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
                .readTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
        }

        fun create(
            endpoints: List<HttpUrl>,
        ): ElasticServiceFactory {
            return when (endpoints.size) {
                0 -> error("Elastic service endpoints has not been provided")
                1 -> ElasticHttpServiceFactory(
                    endpoint = endpoints[0],
                    okHttpClientBuilder = okHttpClientBuilder,
                    gson = gson
                )
                else -> ElasticRoundRobinHttpServiceFactory(
                    endpoints = endpoints,
                    okHttpClientBuilder = okHttpClientBuilder,
                    gson = gson,
                )
            }
        }
    }
}
