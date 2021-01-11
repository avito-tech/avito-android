package com.avito.android.elastic

interface ElasticClient {

    fun sendMessage(
        level: String,
        message: String,
        metadata: Map<String, String>,
        throwable: Throwable?
    )
}
