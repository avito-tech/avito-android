package com.avito.utils.logging

import com.avito.android.elastic.ElasticLog

internal class ElasticDestination(
    private val elasticLog: ElasticLog,
    private val tag: String,
    private val level: String
) : CILoggingDestination {

    override fun write(message: String, throwable: Throwable?) {
        elasticLog.sendMessage(
            tag = tag,
            level = level,
            message = message,
            throwable = throwable
        )
    }

    override fun child(tag: String): CILoggingDestination =
        ElasticDestination(
            elasticLog = elasticLog,
            tag = "${this.tag}_$tag",
            level = level
        )
}
