package com.avito.android.plugin.build_metrics.internal.logger

import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
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
