package com.avito.logger.destination

import com.avito.android.elastic.ElasticClient
import com.avito.android.elastic.ElasticConfig
import com.avito.android.elastic.ElasticFactory
import com.avito.logger.LogLevel
import com.avito.logger.LoggerMetadata
import com.avito.logger.LoggingDestination

internal class ElasticDestination(
    private val config: ElasticConfig,
    private val metadata: LoggerMetadata
) : LoggingDestination {

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

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        client().sendMessage(
            level = level.name,
            message = message,
            metadata = metadata.toMap(),
            throwable = throwable
        )
    }

    private fun LoggerMetadata.toMap(): Map<String, String> {
        val result = mutableMapOf("tag" to tag)

        if (!pluginName.isNullOrBlank()) {
            result["plugin_name"] = pluginName
        }

        if (!projectPath.isNullOrBlank()) {
            result["project_path"] = projectPath
        }

        if (!taskName.isNullOrBlank()) {
            result["task_name"] = taskName
        }

        return result
    }
}
