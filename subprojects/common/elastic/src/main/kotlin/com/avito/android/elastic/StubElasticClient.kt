package com.avito.android.elastic

object StubElasticClient : ElasticClient {
    override fun sendMessage(tag: String, level: String, message: String, throwable: Throwable?) {
    }
}
