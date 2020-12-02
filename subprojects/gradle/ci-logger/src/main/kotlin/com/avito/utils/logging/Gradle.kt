package com.avito.utils.logging

import com.avito.android.elastic.MultipleEndpointsElastic
import com.avito.android.sentry.sentryConfig
import com.avito.time.DefaultTimeProvider
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import com.avito.utils.gradle.envArgs
import okhttp3.OkHttpClient
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import java.io.File

val Task.ciLogger: CILogger
    get() = provideLogger(project, loggerName = getShortenedTaskPath(this))

val Project.ciLogger: CILogger
    get() = provideLogger(project, loggerName = name)

internal object CILoggerRegistry {

    val loggersCache: MutableMap<String, Entity> = mutableMapOf()

    data class Entity(
        val logger: CILogger,
        val destinationFile: File?
    )
}

private fun provideLogger(project: Project, loggerName: String): CILogger {
    return if (project.buildEnvironment is BuildEnvironment.CI && !project.buildEnvironment.inGradleTestKit) {
        val endpoints: List<String> =
            requireNotNull(
                project.properties["avito.elastic.endpoints"]
                    ?.toString()
                    ?.split("|")
            ) {
                "avito.elastic.endpoints has not been provided"
            }

        val indexPattern: String = requireNotNull(project.properties["avito.elastic.indexpattern"]?.toString()) {
            "avito.elastic.indexpattern has not been provided"
        }

        val elasticLogger = MultipleEndpointsElastic(
            okHttpClient = OkHttpClient(),
            timeProvider = DefaultTimeProvider(),
            endpoints = endpoints,
            indexPattern = indexPattern,
            buildId = project.envArgs.build.id.toString(),
            onError = { msg, e -> project.logger.error(msg, e) }
        )

        CILoggerRegistry.loggersCache.getOrPut(loggerName) {
            defaultCILogger(
                project = project,
                name = loggerName,
                elasticLogger = elasticLogger
            )
        }.logger
    } else {
        localBuildLogger(project.gradle.startParameter.logLevel < LogLevel.LIFECYCLE, loggerName)
    }
}

private val isInvokedFromIde = System.getProperty("isInvokedFromIde")?.toBoolean() ?: false

private fun defaultCILogger(
    project: Project,
    name: String,
    elasticLogger: MultipleEndpointsElastic?
): CILoggerRegistry.Entity {

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

    val sentryConfig = project.sentryConfig

    val sentryHandler = CILoggingHandlerImplementation(
        destination = SentryDestination(sentryConfig.get())
    )

    val logger = CILogger(
        debugHandler = CILoggingCombinedHandler(
            handlers = listOf(
                destinationFileHandler
            ).let {
                if (isInvokedFromIde) {
                    it.plus(onlyMessageStdoutHandler)
                } else {
                    it
                }
            }.let {
                if (elasticLogger != null) {
                    it.plus(elasticHandler(elasticLogger, tag = name, level = "DEBUG"))
                } else {
                    it
                }
            }
        ),
        infoHandler = CILoggingCombinedHandler(
            handlers = listOf(
                onlyMessageStdoutHandler,
                destinationFileHandler
            ).let {
                if (elasticLogger != null) {
                    it.plus(elasticHandler(elasticLogger, tag = name, level = "INFO"))
                } else {
                    it
                }
            }
        ),
        warnHandler = CILoggingCombinedHandler(
            handlers = listOf(
                explicitStdoutHandler,
                destinationFileHandler
            ).let {
                if (elasticLogger != null) {
                    it.plus(elasticHandler(elasticLogger, tag = name, level = "WARNING"))
                } else {
                    it
                }
            }
        ),
        criticalHandler = CILoggingCombinedHandler(
            handlers = listOf(
                explicitStdoutHandler,
                destinationFileHandler,
                sentryHandler
            ).let {
                if (elasticLogger != null) {
                    it.plus(elasticHandler(elasticLogger, tag = name, level = "ERROR"))
                } else {
                    it
                }
            }
        )
    )

    return CILoggerRegistry.Entity(
        logger = logger,
        destinationFile = destinationFile
    )
}

private fun elasticHandler(elasticLog: MultipleEndpointsElastic, tag: String, level: String): CILoggingHandler {
    return CILoggingHandlerImplementation(
        destination = ElasticDestination(
            elastic = elasticLog,
            tag = tag,
            level = level
        )
    )
}

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
