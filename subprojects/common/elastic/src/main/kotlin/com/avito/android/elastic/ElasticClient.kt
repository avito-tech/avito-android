package com.avito.android.elastic

interface ElasticClient {

    fun sendMessage(
        tag: String,
        level: String,
        message: String,
        throwable: Throwable?
    )
}
