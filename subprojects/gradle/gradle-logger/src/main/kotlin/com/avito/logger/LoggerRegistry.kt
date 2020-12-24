package com.avito.logger

import com.avito.android.elastic.ElasticConfig
import com.avito.android.sentry.SentryConfig
import com.avito.logger.destination.ElasticDestination
import com.avito.logger.destination.SentryDestination
import com.avito.logger.destination.Slf4jDestination
import com.avito.logger.formatter.AppendMetadataFormatter
import com.avito.logger.handler.CombinedHandler
import com.avito.logger.handler.DefaultLoggingHandler
import com.avito.logger.handler.LoggingHandler

object LoggerRegistry {

    fun create(
        isCiRun: Boolean,
        tag: String,
        projectPath: String,
        sentryConfig: SentryConfig,
        elasticConfig: ElasticConfig,
        pluginName: String?,
        taskName: String?
    ): Logger = provideLogger(
        isCiRun = isCiRun,
        sentryConfig = sentryConfig,
        elasticConfig = elasticConfig,
        metadata = LoggerMetadata(
            tag = tag,
            pluginName = pluginName,
            projectPath = projectPath,
            taskName = taskName
        )
    )

    private fun provideLogger(
        isCiRun: Boolean,
        sentryConfig: SentryConfig,
        elasticConfig: ElasticConfig,
        metadata: LoggerMetadata
    ): Logger = if (isCiRun) {
        createCiLogger(sentryConfig, elasticConfig, metadata)
    } else {
        createLocalBuildLogger(metadata)
    }

    private fun createCiLogger(
        sentryConfig: SentryConfig,
        elasticConfig: ElasticConfig,
        metadata: LoggerMetadata
    ): Logger {

        val elasticHandler = createElasticHandler(elasticConfig, metadata)

        val sentryHandler = createSentryHandler(sentryConfig, metadata)

        return DefaultLogger(
            debugHandler = elasticHandler,
            infoHandler = elasticHandler,
            warningHandler = elasticHandler,
            criticalHandler = CombinedHandler(
                handlers = listOf(
                    elasticHandler,
                    sentryHandler
                )
            )
        )
    }

    private fun createLocalBuildLogger(metadata: LoggerMetadata): Logger {

        val gradleLoggerHandler = DefaultLoggingHandler(
            formatter = AppendMetadataFormatter(metadata),
            destination = Slf4jDestination(metadata.tag)
        )

        return DefaultLogger(
            debugHandler = gradleLoggerHandler,
            infoHandler = gradleLoggerHandler,
            warningHandler = gradleLoggerHandler,
            criticalHandler = gradleLoggerHandler
        )
    }

    private fun createElasticHandler(
        elasticConfig: ElasticConfig,
        metadata: LoggerMetadata
    ): LoggingHandler = DefaultLoggingHandler(
        destination = ElasticDestination(
            config = elasticConfig,
            metadata = metadata
        )
    )

    private fun createSentryHandler(
        sentryConfig: SentryConfig,
        metadata: LoggerMetadata
    ): LoggingHandler = DefaultLoggingHandler(
        destination = SentryDestination(sentryConfig, metadata)
    )
}
