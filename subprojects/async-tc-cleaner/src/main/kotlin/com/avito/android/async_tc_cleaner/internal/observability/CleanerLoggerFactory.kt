package com.avito.android.async_tc_cleaner.internal.observability

import com.avito.android.async_tc_cleaner.internal.config.Config
import com.avito.android.async_tc_cleaner.internal.config.ElasticSettings
import com.avito.android.elastic.ElasticClientFactory
import com.avito.android.elastic.ElasticConfig
import com.avito.logger.LogLevel
import com.avito.logger.LoggerFactory
import com.avito.logger.LoggerFactoryBuilder
import com.avito.logger.destination.ElasticLoggingHandlerProvider
import com.avito.logger.handler.PrintlnLoggingHandlerProvider
import com.avito.logger.metadata.LoggerMetadata
import com.avito.logger.metadata.LoggerMetadataProvider
import com.avito.logger.metadata.runtime.NoOpLoggerRuntimeMetadataProvider

internal object CleanerLoggerFactory {

    fun create(config: Config): LoggerFactory {
        val builder = LoggerFactoryBuilder()
            .metadataProvider(CleanerLoggerMetadataProvider(config))
            .addLoggingHandlerProvider(
                PrintlnLoggingHandlerProvider(
                    acceptedLogLevel = LogLevel.INFO,
                    printStackTrace = true,
                    printMessageTime = false,
                )
            )

        val elastic = config.elastic
        if (elastic != null) {
            builder.addLoggingHandlerProvider(elasticLoggingHandler(elastic, config))
        }
        return builder.build()
    }

    private fun elasticLoggingHandler(settings: ElasticSettings, config: Config): ElasticLoggingHandlerProvider {
        val elasticConfig = ElasticConfig.Enabled(
            endpoints = settings.endpoints,
            indexName = settings.index,
            sourceType = "async-tc-cleaner",
            sourceId = config.pod,
            authApiKey = settings.apiKey,
        )

        return ElasticLoggingHandlerProvider(
            acceptedLogLevel = LogLevel.INFO,
            elasticClient = ElasticClientFactory.provide(elasticConfig),
            runtimeMetadataProvider = NoOpLoggerRuntimeMetadataProvider,
        )
    }
}

private class CleanerLoggerMetadataProvider(
    private val config: Config,
) : LoggerMetadataProvider {

    override fun provide(tag: String): LoggerMetadata = CleanerLoggerMetadata(
        tag = tag,
        node = config.node,
        pod = config.pod,
    )
}

private class CleanerLoggerMetadata(
    private val tag: String,
    private val node: String,
    private val pod: String,
) : LoggerMetadata {

    override val asMessagePrefix: String = "[$tag]"

    override fun asMap(): Map<String, String> = mapOf(
        "tag" to tag,
        "node" to node,
        "pod" to pod,
    )
}
