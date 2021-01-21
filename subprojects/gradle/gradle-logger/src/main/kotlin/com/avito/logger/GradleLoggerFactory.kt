package com.avito.logger

import com.avito.android.elastic.ElasticConfig
import com.avito.android.sentry.SentryConfig
import com.avito.android.sentry.sentryConfig
import com.avito.logger.destination.Slf4jDestination
import com.avito.logger.formatter.AppendMetadataFormatter
import com.avito.logger.handler.CombinedHandler
import com.avito.logger.handler.DefaultLoggingHandler
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.Serializable

class GradleLoggerFactory(
    private val isCiRun: Boolean,
    private val sentryConfig: SentryConfig,
    private val elasticConfig: ElasticConfig,
    private val projectPath: String,
    private val pluginName: String? = null,
    private val taskName: String? = null
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

        val defaultHandler = when (elasticConfig) {
            is ElasticConfig.Disabled ->
                DefaultLoggingHandler(
                    destination = Slf4jDestination(metadata.tag)
                )
            is ElasticConfig.Enabled ->
                DefaultLoggingHandler(
                    destination = ElasticDestinationFactory.create(elasticConfig, metadata)
                )
        }

        val sentryHandler = DefaultLoggingHandler(
            destination = SentryDestinationFactory.create(sentryConfig, metadata)
        )

        return DefaultLogger(
            debugHandler = defaultHandler,
            infoHandler = defaultHandler,
            warningHandler = defaultHandler,
            criticalHandler = CombinedHandler(
                handlers = listOf(
                    defaultHandler,
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

    companion object {

        inline fun <reified T : Task> getLogger(task: T): Logger = fromTask(task).create<T>()

        inline fun <reified T : Plugin<*>> getLogger(plugin: T, project: Project): Logger =
            fromPlugin(plugin, project).create<T>()

        fun fromTask(task: Task): GradleLoggerFactory = fromProject(
            project = task.project,
            taskName = task.name
        )

        fun fromPlugin(
            plugin: Plugin<*>,
            project: Project
        ): GradleLoggerFactory = fromProject(
            project = project,
            pluginName = plugin.javaClass.simpleName
        )

        fun fromProject(
            project: Project,
            pluginName: String? = null,
            taskName: String? = null
        ): GradleLoggerFactory = GradleLoggerFactory(
            isCiRun = project.isCiRun(),
            sentryConfig = project.sentryConfig.get(),
            elasticConfig = ElasticConfigFactory.config(project),
            projectPath = project.path,
            pluginName = pluginName,
            taskName = taskName
        )

        private fun Project.isCiRun(): Boolean =
            project.buildEnvironment is BuildEnvironment.CI && !project.buildEnvironment.inGradleTestKit
    }
}
