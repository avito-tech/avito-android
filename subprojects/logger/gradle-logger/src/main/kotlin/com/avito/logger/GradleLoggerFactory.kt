package com.avito.logger

import com.avito.android.elastic.ElasticConfig
import com.avito.android.sentry.SentryConfig
import com.avito.android.sentry.sentryConfig
import com.avito.logger.destination.Slf4jDestination
import com.avito.logger.destination.VerboseDestination
import com.avito.logger.destination.VerboseMode
import com.avito.logger.formatter.AppendMetadataFormatter
import com.avito.logger.handler.CombinedHandler
import com.avito.logger.handler.DefaultLoggingHandler
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.configuration.ShowStacktrace
import java.io.Serializable
import java.util.Locale

public class GradleLoggerFactory(
    private val isCiRun: Boolean,
    private val sentryConfig: SentryConfig,
    private val elasticConfig: ElasticConfig,
    private val projectPath: String,
    private val pluginName: String? = null,
    private val taskName: String? = null,
    private val verboseMode: VerboseMode?,
) : LoggerFactory, Serializable {

    override fun create(tag: String): Logger = provideLogger(
        isCiRun = isCiRun,
        sentryConfig = sentryConfig,
        elasticConfig = elasticConfig,
        metadata = LoggerMetadata(
            tag = tag,
            pluginName = pluginName,
            projectPath = projectPath,
            taskName = taskName
        ),
        verboseMode = verboseMode
    )

    private fun provideLogger(
        isCiRun: Boolean,
        sentryConfig: SentryConfig,
        elasticConfig: ElasticConfig,
        metadata: LoggerMetadata,
        verboseMode: VerboseMode?
    ): Logger = if (isCiRun) {
        createCiLogger(sentryConfig, elasticConfig, metadata, verboseMode)
    } else {
        createLocalBuildLogger(metadata, verboseMode)
    }

    private fun createCiLogger(
        sentryConfig: SentryConfig,
        elasticConfig: ElasticConfig,
        metadata: LoggerMetadata,
        verboseMode: VerboseMode?
    ): Logger {

        val defaultHandler = when (elasticConfig) {
            is ElasticConfig.Disabled -> {

                val destination: LoggingDestination = if (verboseMode != null) {
                    VerboseDestination(verboseMode)
                } else {
                    Slf4jDestination(metadata.tag)
                }

                DefaultLoggingHandler(destination = destination)
            }
            is ElasticConfig.Enabled ->
                DefaultLoggingHandler(
                    destination = ElasticDestinationFactory.create(elasticConfig, metadata)
                )
        }

        val sentryHandler = DefaultLoggingHandler(
            destination = SentryDestinationFactory.create(sentryConfig, metadata)
        )

        val errorHandler = CombinedHandler(
            handlers = listOf(
                defaultHandler,
                sentryHandler
            )
        )

        return DefaultLogger(
            debugHandler = defaultHandler,
            infoHandler = defaultHandler,
            warningHandler = errorHandler,
            criticalHandler = errorHandler
        )
    }

    private fun createLocalBuildLogger(
        metadata: LoggerMetadata,
        verboseMode: VerboseMode?
    ): Logger {

        val destination: LoggingDestination = if (verboseMode != null) {
            VerboseDestination(verboseMode)
        } else {
            Slf4jDestination(metadata.tag)
        }

        val gradleLoggerHandler = DefaultLoggingHandler(
            formatter = AppendMetadataFormatter(metadata),
            destination = destination
        )

        return DefaultLogger(
            debugHandler = gradleLoggerHandler,
            infoHandler = gradleLoggerHandler,
            warningHandler = gradleLoggerHandler,
            criticalHandler = gradleLoggerHandler
        )
    }

    public companion object {

        public inline fun <reified T : Plugin<*>> getLogger(plugin: T, project: Project): Logger =
            fromPlugin(plugin, project).create<T>()

        public fun fromPlugin(
            plugin: Plugin<*>,
            project: Project
        ): GradleLoggerFactory = fromProject(
            project = project,
            pluginName = plugin.javaClass.simpleName
        )

        public fun fromProject(
            project: Project,
            pluginName: String? = null,
            taskName: String? = null
        ): GradleLoggerFactory = GradleLoggerFactory(
            isCiRun = project.isCiRun(),
            sentryConfig = project.sentryConfig.get(),
            elasticConfig = ElasticConfigFactory.config(project),
            projectPath = project.path,
            pluginName = pluginName,
            taskName = taskName,
            verboseMode = getVerbosity(project)?.let { VerboseMode(it, doPrintStackTrace(project)) }
        )

        private fun getVerbosity(project: Project): LogLevel? {
            return project.providers
                .gradleProperty("avito.logging.verbosity")
                .forUseAtConfigurationTime()
                .map { value ->
                    try {
                        LogLevel.valueOf(value.toUpperCase(Locale.getDefault()))
                    } catch (e: Throwable) {
                        throw IllegalArgumentException(
                            "`avito.logging.verbosity` should be one of: " +
                                "${LogLevel.values().map { it.name }} but was $value"
                        )
                    }
                }
                .orNull
        }

        private fun doPrintStackTrace(project: Project): Boolean {
            val showStacktrace = project.gradle.startParameter.showStacktrace
            return showStacktrace == ShowStacktrace.ALWAYS || showStacktrace == ShowStacktrace.ALWAYS_FULL
        }

        private fun Project.isCiRun(): Boolean =
            project.buildEnvironment is BuildEnvironment.CI && !project.buildEnvironment.inGradleTestKit
    }
}
