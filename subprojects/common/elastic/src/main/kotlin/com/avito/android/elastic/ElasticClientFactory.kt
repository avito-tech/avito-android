package com.avito.android.elastic

import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ElasticClientFactory {

    private const val defaultTimeoutSec = 10L

    private val timeProvider: TimeProvider by lazy { DefaultTimeProvider() }

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
            .readTimeout(defaultTimeoutSec, TimeUnit.SECONDS)
            .build()
    }

    private val cache = mutableMapOf<ElasticConfig, HttpElasticClient>()

    fun provide(config: ElasticConfig, onError: (String, Throwable?) -> Unit): ElasticClient {
        return when (config) {
            is ElasticConfig.Disabled -> StubElasticClient
            is ElasticConfig.Enabled -> {
                cache.getOrPut(
                    key = config,
                    defaultValue = {
                        HttpElasticClient(
                            okHttpClient = httpClient,
                            timeProvider = timeProvider,
                            endpoints = config.endpoints,
                            indexPattern = config.indexPattern,
                            buildId = config.buildId,
                            onError = onError
                        )
                    }
                )
            }
        }
    }
}
