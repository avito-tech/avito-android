package com.avito.logger.destination

import com.avito.android.elastic.ElasticClient
import com.avito.logger.LogLevel
import com.avito.logger.handler.LogLevelLoggingHandler

public class ElasticLoggingHandler(
    acceptedLogLevel: LogLevel,
    private val client: ElasticClient,
    private val metadata: Map<String, String>
) : LogLevelLoggingHandler(acceptedLogLevel) {

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        client.sendMessage(
            level = level.name,
            message = message,
            metadata = metadata,
            throwable = error
        )
    }
}
