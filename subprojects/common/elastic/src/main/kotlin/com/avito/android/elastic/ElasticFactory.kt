package com.avito.android.elastic

import com.avito.time.DefaultTimeProvider
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ElasticFactory {

    private const val defaultTimeoutSec = 10L

    fun create(config: ElasticConfig, onError: (String, Throwable?) -> Unit): ElasticClient {
        return when (config) {
            is ElasticConfig.Disabled -> StubElasticClient
            is ElasticConfig.Enabled -> {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
                    .readTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
                    .build()

                HttpElasticClient(
                    okHttpClient = okHttpClient,
                    timeProvider = DefaultTimeProvider(),
                    endpoints = config.endpoints,
                    indexPattern = config.indexPattern,
                    buildId = config.buildId,
                    onError = onError
                )
            }
        }
    }
}
