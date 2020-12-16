package com.avito.utils.logging

import com.avito.android.elastic.ElasticConfig
import com.avito.android.sentry.sentryConfig
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import java.io.File

val Task.ciLogger: CILogger
    get() = provideLogger(project, loggerName = getShortenedTaskPath(this))

val Project.ciLogger: CILogger
    get() = provideLogger(project, loggerName = name)

internal object CILoggerRegistry {

    val loggersCache = mutableMapOf<String, CILogger>()

}

private fun provideLogger(project: Project, loggerName: String): CILogger {
    return if (project.buildEnvironment is BuildEnvironment.CI && !project.buildEnvironment.inGradleTestKit) {
        CILoggerRegistry.loggersCache.getOrPut(loggerName) {
            defaultCILogger(
                project = project,
                name = loggerName
            )
        }
    } else {
        localBuildLogger(project.gradle.startParameter.logLevel < LogLevel.LIFECYCLE, loggerName)
    }
}

private val isInvokedFromIde = System.getProperty("isInvokedFromIde")?.toBoolean() ?: false

private fun defaultCILogger(
    project: Project,
    name: String
): CILogger {

    val destinationFileName = "${project.rootDir}/outputs/ci/$name.txt"

    val destinationFile = File(destinationFileName)

    val destinationFileHandler = CILoggingHandlerImplementation(
        formatter = AppendDateTimeFormatter(),
        destination = FileDestination(destinationFile)
    )

    val onlyMessageStdoutHandler = CILoggingHandlerImplementation(
        formatter = AppendPrefixFormatter(prefix = name),
        destination = OnlyMessageStdoutDestination
    )

    val explicitStdoutHandler = CILoggingHandlerImplementation(
        formatter = AppendPrefixFormatter(prefix = name),
        destination = StdoutDestination
    )

    val explicitStderrHandler = CILoggingHandlerImplementation(
        formatter = AppendPrefixFormatter(prefix = name),
        destination = StderrDestination
    )

    val sentryConfig = project.sentryConfig

    val sentryHandler = CILoggingHandlerImplementation(
        destination = SentryDestination(sentryConfig.get())
    )
    val elasticConfig = ElasticConfigFactory.config(project)

    return CILogger(
        debugHandler = CILoggingCombinedHandler(
            handlers = listOf(
                destinationFileHandler,
                elasticHandler(elasticConfig, tag = name, level = "DEBUG")
            ).let {
                if (isInvokedFromIde) {
                    it.plus(onlyMessageStdoutHandler)
                } else {
                    it
                }
            }
        ),
        infoHandler = CILoggingCombinedHandler(
            handlers = listOf(
                onlyMessageStdoutHandler,
                destinationFileHandler,
                elasticHandler(elasticConfig, tag = name, level = "INFO")
            )
        ),
        warnHandler = CILoggingCombinedHandler(
            handlers = listOf(
                explicitStdoutHandler,
                destinationFileHandler,
                elasticHandler(elasticConfig, tag = name, level = "WARNING")
            )
        ),
        criticalHandler = CILoggingCombinedHandler(
            handlers = listOf(
                explicitStderrHandler,
                destinationFileHandler,
                elasticHandler(elasticConfig, tag = name, level = "ERROR"),
                sentryHandler
            )
        )
    )
}

private fun elasticHandler(
    elasticConfig: ElasticConfig,
    tag: String,
    level: String
): CILoggingHandler = CILoggingHandlerImplementation(
    destination = ElasticDestination(
        config = elasticConfig,
        tag = tag,
        level = level
    )
)

private fun localBuildLogger(
    debug: Boolean,
    name: String
): CILogger {

    val gradleDebugLogger = if (debug) {
        CILoggingHandlerImplementation(
            formatter = AppendPrefixFormatter(name),
            destination = StdoutDestination
        )
    } else {
        NothingLoggingHandler
    }

    val stdoutHandler = CILoggingHandlerImplementation(
        formatter = AppendPrefixFormatter(name),
        destination = StdoutDestination
    )

    return CILogger(
        debugHandler = gradleDebugLogger,
        infoHandler = stdoutHandler,
        criticalHandler = stdoutHandler,
        warnHandler = stdoutHandler
    )
}

private fun getShortenedTaskPath(task: Task) = "${task.project.name}:${task.name}" // TODO: use path to make unique name
