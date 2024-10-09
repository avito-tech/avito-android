package com.avito.android.elastic

import com.google.gson.GsonBuilder
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import java.net.URL

internal class ElasticApiProvider(
    private val gson: GsonBuilder,
    private val okHttpClientBuilder: OkHttpClient.Builder,
) {

    fun provide(
        endpoints: List<URL>,
    ): ElasticApi {
        val httpUrls = endpoints.map { endpoint ->
            requireNotNull(endpoint.toHttpUrlOrNull()) {
                "Failed to map to http url. Endpoint can't be null"
            }
        }
        return when (httpUrls.size) {
            0 -> error("Elastic service endpoints has not been provided")
            1 -> ElasticHttpServiceFactory(
                endpoint = httpUrls[0],
                okHttpClientBuilder = okHttpClientBuilder,
                gson = gson
            ).provide()

            else -> ElasticRoundRobinHttpServiceFactory(
                endpoints = httpUrls,
                okHttpClientBuilder = okHttpClientBuilder,
                gson = gson,
            ).provide()
        }
    }
}
