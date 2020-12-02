package com.avito.utils.logging

import com.avito.android.elastic.Elastic

internal class ElasticDestination(
    private val elastic: Elastic,
    private val tag: String,
    private val level: String
) : CILoggingDestination {

    override fun write(message: String, throwable: Throwable?) {
        elastic.sendMessage(
            tag = tag,
            level = level,
            message = message,
            throwable = throwable
        )
    }

    override fun child(tag: String): CILoggingDestination =
        ElasticDestination(
            elastic = elastic,
            tag = "${this.tag}_$tag",
            level = level
        )
}
