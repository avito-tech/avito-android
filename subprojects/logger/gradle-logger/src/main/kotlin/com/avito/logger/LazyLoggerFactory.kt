package com.avito.logger

import org.gradle.api.provider.Provider

/**
 * Hack to provide loggerFactory on first usage.
 */
internal class LazyLoggerFactory(
    loggerFactoryProvider: Provider<LoggerFactory>
) : LoggerFactory {

    private val delegate by lazy { loggerFactoryProvider.get() }

    override fun create(tag: String): Logger {
        return LazyLogger {
            delegate.create(tag)
        }
    }
}
