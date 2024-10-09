package com.avito.android.elastic

import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient

public class ElasticClientProvider(
    private val config: ElasticConfig.Enabled,
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory,
    okHttpClient: OkHttpClient.Builder,
    gsonBuilder: GsonBuilder,
) {

    private val elasticApi = ElasticApiProvider(
        gson = gsonBuilder,
        okHttpClientBuilder = okHttpClient
    )

    public fun provide(): ElasticClient {
        return HttpElasticClient(
            elasticApi = elasticApi.provide(config.endpoints),
            timeProvider = timeProvider,
            indexName = config.indexName,
            sourceType = config.sourceType,
            sourceId = config.sourceId,
            authApiKey = config.authApiKey,
            loggerFactory = loggerFactory
        )
    }
}
