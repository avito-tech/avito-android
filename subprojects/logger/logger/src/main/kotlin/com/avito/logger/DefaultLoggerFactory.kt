package com.avito.logger

import com.avito.logger.handler.CompositeLoggingHandler
import com.avito.logger.handler.LoggingHandlerProvider

internal class DefaultLoggerFactory(
    private val formatter: LoggingFormatter?,
    private val metadataProvider: LoggerMetadataProvider,
    private val handlerProviders: List<LoggingHandlerProvider>
) : LoggerFactory {

    init {
        check(handlerProviders.isNotEmpty()) {
            "handler providers must contain at least one provider"
        }
    }

    override fun create(tag: String): Logger {
        val metadata = metadataProvider.provide(tag)
        val handlers = handlerProviders.map { it.provide(metadata) }
        val handler = CompositeLoggingHandler(handlers)
        return LoggerImpl(
            handler = if (formatter != null) {
                FormatterLoggingHandler(formatter, handler, metadata)
            } else {
                handler
            },
        )
    }
}

public class LoggerFactoryBuilder {

    private var formatter: LoggingFormatter = LoggingFormatter.NoOpFormatter
    private var metadataProvider: LoggerMetadataProvider = TagLoggerMetadataProvider
    private val handlerProviders = mutableListOf<LoggingHandlerProvider>()

    public fun formatter(formatter: LoggingFormatter): LoggerFactoryBuilder {
        this.formatter = formatter
        return this
    }

    public fun metadataProvider(provider: LoggerMetadataProvider): LoggerFactoryBuilder {
        this.metadataProvider = provider
        return this
    }

    public fun addLoggingHandlerProvider(
        provider: LoggingHandlerProvider
    ): LoggerFactoryBuilder {
        handlerProviders.add(provider)
        return this
    }

    public fun newBuilder(): LoggerFactoryBuilder {
        return LoggerFactoryBuilder().also { newBuilder ->
            newBuilder.formatter = formatter
            newBuilder.metadataProvider = metadataProvider
            newBuilder.handlerProviders.addAll(handlerProviders)
        }
    }

    public fun build(): LoggerFactory {
        return DefaultLoggerFactory(
            formatter = formatter,
            metadataProvider = metadataProvider,
            handlerProviders = handlerProviders.toList()
        )
    }
}
