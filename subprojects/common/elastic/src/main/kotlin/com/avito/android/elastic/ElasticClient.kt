package com.avito.android.elastic

public interface ElasticClient {

    public fun sendMessage(
        level: String,
        message: String,
        metadata: Map<String, String>,
        throwable: Throwable?
    )
}
