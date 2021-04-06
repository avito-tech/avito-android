package com.avito.android.sentry

import com.avito.git.Git
import com.avito.kotlin.dsl.lazyProperty
import com.avito.logger.DefaultLogger
import com.avito.logger.LogLevel
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.LoggingDestination
import com.avito.logger.handler.DefaultLoggingHandler
import com.avito.utils.gradle.Environment
import com.avito.utils.gradle.internal.EnvironmentInfoImpl
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * Use [Project.environmentInfo] to gain instance
 */
interface EnvironmentInfo { // TODO: merge with BuildEnvironment and EnvArgs
    val node: String?
    val environment: Environment
    fun teamcityBuildId(): String?
}

fun Project.environmentInfo(): Provider<EnvironmentInfo> = lazyProperty("ENVIRONMENT_INFO_PROVIDER") { project ->
    project.providers.provider {

        val loggerFactory = SimpleLoggerFactory()

        val git = Git.Impl(
            rootDir = project.rootDir,
            loggerFactory = loggerFactory
        )
        EnvironmentInfoImpl(project, git)
    }
}

/**
 * Can't depend on GradleLoggerFactory, because of cyclic dependency:
 *  - Gradle logger depends on sentry to send critical events
 */
private class SimpleLoggerFactory : LoggerFactory {

    override fun create(tag: String): Logger {

        val defaultHandler = DefaultLoggingHandler(destination = Slf4jDestination(tag))

        return DefaultLogger(
            debugHandler = defaultHandler,
            infoHandler = defaultHandler,
            warningHandler = defaultHandler,
            criticalHandler = defaultHandler
        )
    }
}

/**
 * Can't reuse this class from Gradle logger
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
