package com.avito.android.elastic

public class StubElasticClient : ElasticClient {

    override fun sendMessage(
        level: String,
        message: String,
        metadata: Map<String, String>,
        throwable: Throwable?
    ) {
        // empty
    }
}
