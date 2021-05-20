package com.avito.logger

import com.avito.android.sentry.SentryConfig
import com.avito.logger.destination.SentryDestination

internal object SentryDestinationFactory {

    fun create(
        sentryConfig: SentryConfig,
        metadata: LoggerMetadata
    ): LoggingDestination = SentryDestination(
        config = sentryConfig,
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
