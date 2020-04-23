package com.avito.utils.logging

import com.avito.android.sentry.sentryConfig
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import java.io.File

internal object CILoggerRegistry {

    val loggersCache: MutableMap<String, Entity> = mutableMapOf()

    data class Entity(
        val logger: CILogger,
        val destinationFile: File?
    )
}

val Task.ciLogger: CILogger
    get() = provideLogger(project, loggerName = getShortenedTaskPath(this))

val Project.ciLogger: CILogger
    get() = provideLogger(project, loggerName = name)

private fun provideLogger(project: Project, loggerName: String): CILogger {
    return if (project.buildEnvironment is BuildEnvironment.CI && !project.buildEnvironment.inGradleTestKit) {
        CILoggerRegistry.loggersCache.getOrPut(loggerName) {
            defaultCILogger(
                project = project,
                name = loggerName
            )
        }.logger
    } else {
        localBuildLogger(project.gradle.startParameter.logLevel < LogLevel.LIFECYCLE)
    }
}

private fun defaultCILogger(
    project: Project,
    name: String
): CILoggerRegistry.Entity {
    val destinationFileName = "${project.rootDir}/outputs/ci/$name.txt"
    val destinationFile = File(destinationFileName)

    val destinationFileHandler = CILoggingHandlerImplementation(
        formatter = AppendDateTimeFormatter(),
        destination = FileDestination(destinationFile)
    )
    val stdoutHandler = CILoggingHandlerImplementation(
        formatter = AppendPrefixFormatter(prefix = name),
        destination = StdoutDestination
    )
    val sentryConfig = project.sentryConfig
    val sentryHandler = CILoggingHandlerImplementation(
        destination = SentryDestination(sentryConfig.get())
    )

    val logger = CILogger(
        infoHandler = CILoggingCombinedHandler(
            handlers = listOf(
                stdoutHandler,
                destinationFileHandler
            )
        ),
        criticalHandler = CILoggingCombinedHandler(
            handlers = listOf(
                stdoutHandler,
                destinationFileHandler,
                sentryHandler
            )
        ),
        debugHandler = CILoggingCombinedHandler(
            handlers = listOf(
                destinationFileHandler
            )
        )
    )

    return CILoggerRegistry.Entity(
        logger = logger,
        destinationFile = destinationFile
    )
}

private fun localBuildLogger(debug: Boolean): CILogger {

    val gradleDebugLogger = if (debug) {
        CILoggingHandlerImplementation(
            destination = StdoutDestination
        )
    } else {
        NothingLoggingHandler
    }

    val gradleLifecycleLogger = CILoggingHandlerImplementation(
        destination = StdoutDestination
    )

    val gradleErrorLogger = CILoggingHandlerImplementation(
        destination = StdoutDestination
    )

    return CILogger(
        debugHandler = gradleDebugLogger,
        infoHandler = gradleLifecycleLogger,
        criticalHandler = gradleErrorLogger
    )
}

private fun getShortenedTaskPath(task: Task) = "${task.project.name}:${task.name}" // TODO: use path to make unique name
