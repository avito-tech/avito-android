package com.avito.android.elastic

internal object StubElasticClient : ElasticClient {

    override fun sendMessage(
        level: String,
        message: String,
        metadata: Map<String, String>,
        throwable: Throwable?
    ) {
    }
}
