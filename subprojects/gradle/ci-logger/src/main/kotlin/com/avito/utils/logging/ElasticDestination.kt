package com.avito.utils.logging

import com.avito.android.elastic.ElasticClient
import com.avito.android.elastic.ElasticConfig
import com.avito.android.elastic.ElasticFactory

internal class ElasticDestination(
    private val config: ElasticConfig,
    private val tag: String,
    private val level: String
) : CILoggingDestination {

    @Transient
    private lateinit var _client: ElasticClient

    private fun client(): ElasticClient {
        if (!::_client.isInitialized) {
            _client = ElasticFactory.create(config) { msg, error ->
                System.err.println(msg)
                error?.also { System.err.println(it) }
            }
        }
        return _client
    }

    override fun write(message: String, throwable: Throwable?) {
        client().sendMessage(
            tag = tag,
            level = level,
            message = message,
            throwable = throwable
        )
    }

    override fun child(tag: String): CILoggingDestination =
        ElasticDestination(
            config = config,
            tag = "${this.tag}_$tag",
            level = level
        )
}
