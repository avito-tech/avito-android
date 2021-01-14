package com.avito.logger.destination

import com.avito.android.elastic.ElasticClient
import com.avito.android.elastic.ElasticClientFactory
import com.avito.android.elastic.ElasticConfig
import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination

class ElasticDestination(
    private val config: ElasticConfig,
    private val metadata: Map<String, String>
) : LoggingDestination {

    @Transient
    private lateinit var _client: ElasticClient

    private fun client(): ElasticClient {
        if (!::_client.isInitialized) {
            _client = ElasticClientFactory.provide(config) { msg, error ->
                System.err.println(msg)
                error?.also { System.err.println(it) }
            }
        }
        return _client
    }

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        client().sendMessage(
            level = level.name,
            message = message,
            metadata = metadata,
            throwable = throwable
        )
    }
}
