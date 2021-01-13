package com.avito.logger

import com.avito.android.elastic.ElasticConfig
import com.avito.logger.destination.ElasticDestination

internal object ElasticDestinationFactory {

    fun create(
        elasticConfig: ElasticConfig,
        metadata: LoggerMetadata
    ): LoggingDestination = ElasticDestination(
        config = elasticConfig,
        metadata = metadata.toMap()
    )

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
