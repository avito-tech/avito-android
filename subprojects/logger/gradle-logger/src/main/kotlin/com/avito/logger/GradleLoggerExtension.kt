package com.avito.logger

import com.avito.android.elastic.ElasticConfig
import com.avito.android.sentry.SentryConfig
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import java.io.Serializable

public abstract class GradleLoggerExtension {

    public data class PrintlnMode(
        val level: LogLevel,
        val printStackTrace: Boolean
    ) : Serializable

    public data class Sentry(
        val level: LogLevel,
        val config: SentryConfig
    ) : Serializable

    public data class Elastic(
        val level: LogLevel,
        val config: ElasticConfig
    ) : Serializable

    public abstract val fileHandler: Property<LogLevel>
    public abstract val fileHandlerRootDir: DirectoryProperty
    internal abstract val printlnHandler: Property<PrintlnMode>
    internal abstract val sentryHandler: Property<Sentry>
    internal abstract val elasticHandler: Property<Elastic>
    internal abstract val finalized: Property<Boolean>

    public fun printlnHandler(printStackTrace: Boolean, level: LogLevel) {
        printlnHandler.set(PrintlnMode(level, printStackTrace))
    }

    public fun sentryHandler(config: SentryConfig, level: LogLevel) {
        sentryHandler.set(Sentry(level, config))
    }

    public fun elasticHandler(elasticConfig: ElasticConfig, level: LogLevel) {
        elasticHandler.set(Elastic(level, elasticConfig))
    }

    internal fun finalizeValues() {
        fileHandler.finalizeValue()
        fileHandlerRootDir.finalizeValue()
        printlnHandler.finalizeValue()
        sentryHandler.finalizeValue()
        elasticHandler.finalizeValue()
        finalized.set(true)
        finalized.finalizeValue()
    }
}
