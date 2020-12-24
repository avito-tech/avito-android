@file:Suppress("UnstableApiUsage", "MoveVariableDeclarationIntoWhen")

package com.avito.git

import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.lazyProperty
import com.avito.logger.DefaultLogger
import com.avito.logger.LogLevel
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.LoggingDestination
import com.avito.logger.LoggingFormatter
import com.avito.logger.handler.DefaultLoggingHandler
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

/**
 * Warning! used in build.gradle files
 */
fun Project.gitState(): Provider<GitState> =
    lazyProperty("GIT_STATE_PROVIDER") { project ->
        project.objects.property<GitState>().apply {
            val strategy = project.getOptionalStringProperty("avito.git.state", default = "env")

            val loggerFactory = SimpleLoggerFactory()

            set(
                when (strategy) {
                    "local" -> GitLocalStateImpl.from(
                        project = project,
                        loggerFactory = loggerFactory
                    )
                    "env" -> GitStateFromEnvironment.from(
                        project = project,
                        loggerFactory = loggerFactory
                    )
                    else -> throw RuntimeException("Unknown git state strategy: $strategy")
                }
            )
        }
    }

/**
 * Can't depend on GradleLoggerFactory, because of cyclic dependency:
 *  - gradle logger depends on sentry to send criticals
 *  - sentry depends on this module to get user data
 */
private class SimpleLoggerFactory : LoggerFactory {

    override fun create(tag: String): Logger {

        val defaultHandler = DefaultLoggingHandler(
            destination = Slf4jDestination(tag),
            formatter = AppendClassNameFormatter(tag)
        )

        return DefaultLogger(
            debugHandler = defaultHandler,
            infoHandler = defaultHandler,
            warningHandler = defaultHandler,
            criticalHandler = defaultHandler
        )
    }
}

/**
 * Can't reuse this class from gradle logger
 * and can't move it to common logger, because it needs dependency on slf4j in that case
 */
private class Slf4jDestination(private val className: String) : LoggingDestination {

    @Transient
    private lateinit var _logger: org.slf4j.Logger

    private fun logger(): org.slf4j.Logger {
        if (!::_logger.isInitialized) {
            _logger = org.slf4j.LoggerFactory.getLogger(className)
        }
        return _logger
    }

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        with(logger()) {
            when (level) {
                LogLevel.DEBUG -> debug(message, throwable)
                LogLevel.INFO -> info(message, throwable)
                LogLevel.WARNING -> warn(message, throwable)
                LogLevel.CRITICAL -> error(message, throwable)
            }
        }
    }
}

private class AppendClassNameFormatter(private val className: String) : LoggingFormatter {

    override fun format(message: String): String = "[$className] $message"
}
