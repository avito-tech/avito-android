@file:Suppress("UnstableApiUsage")

package com.avito.android.sentry

import com.avito.git.Git
import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.lazyProperty
import com.avito.logger.DefaultLogger
import com.avito.logger.LogLevel
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.LoggingDestination
import com.avito.logger.handler.DefaultLoggingHandler
import com.avito.utils.gradle.buildEnvironment
import io.sentry.SentryClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

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

val Project.sentry: Provider<SentryClient> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    Providers.of(sentryClient(project.sentryConfig.get()))
}

val Project.sentryConfig: Provider<SentryConfig> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    Providers.of(from(project))
}

private fun from(project: Project): SentryConfig {
    return if (project.getBooleanProperty("avito.sentry.enabled")) {
        val buildEnv = project.buildEnvironment
        val info = project.environmentInfo().get()
        val tags = mutableMapOf<String, String>()

        val buildId = info.teamcityBuildId()

        val buildIdTag = "build_id"

        if (!buildId.isNullOrBlank()) {
            tags[buildIdTag] = buildId
        }

        val config = SentryConfig.Enabled(
            dsn = project.getMandatoryStringProperty("avito.sentry.dsn"),
            environment = buildEnv::class.java.simpleName,
            serverName = info.node ?: "unknown",
            release = info.commit ?: "unknown",
            tags = tags
        )

        val projectUrl = project.getOptionalStringProperty("avito.sentry.projectUrl")

        if (!projectUrl.isNullOrBlank() && !buildId.isNullOrBlank()) {
            project.gradle.buildFinished {

                val url = projectUrl.toHttpUrlOrNull()
                    ?.newBuilder()
                    ?.addQueryParameter("query", "$buildIdTag:$buildId")
                    ?.build()
                    ?.toString()

                project.logger.lifecycle("Build errors: $url")
            }
        }

        config
    } else {
        SentryConfig.Disabled
    }
}

/**
 * Can't depend on GradleLoggerFactory, because of cyclic dependency:
 *  - gradle logger depends on sentry to send criticals
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
