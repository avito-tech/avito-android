package com.avito.utils.logging

import com.avito.android.elastic.ElasticClient

internal class ElasticDestination(
    private val elasticClient: ElasticClient,
    private val tag: String,
    private val level: String
) : CILoggingDestination {

    override fun write(message: String, throwable: Throwable?) {
        elasticClient.sendMessage(
            tag = tag,
            level = level,
            message = message,
            throwable = throwable
        )
    }

    override fun child(tag: String): CILoggingDestination =
        ElasticDestination(
            elasticClient = elasticClient,
            tag = "${this.tag}_$tag",
            level = level
        )
}
