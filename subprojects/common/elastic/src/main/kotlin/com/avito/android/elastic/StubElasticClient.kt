package com.avito.android.elastic

object StubElasticClient : ElasticClient {

    override fun sendMessage(
        level: String,
        message: String,
        metadata: Map<String, String>,
        throwable: Throwable?
    ) {
    }
}
