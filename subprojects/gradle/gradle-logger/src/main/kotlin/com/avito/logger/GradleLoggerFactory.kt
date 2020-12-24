package com.avito.logger

import com.avito.android.elastic.ElasticConfig
import com.avito.android.sentry.SentryConfig
import com.avito.android.sentry.sentryConfig
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

    override fun create(tag: String): Logger = LoggerRegistry.create(
        isCiRun = isCiRun,
        tag = tag,
        projectPath = projectPath,
        sentryConfig = sentryConfig,
        elasticConfig = elasticConfig,
        pluginName = pluginName,
        taskName = taskName
    )

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
