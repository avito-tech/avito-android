package com.avito.utils.logging

import com.avito.android.elastic.ElasticLog
import com.avito.android.sentry.sentryConfig
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import com.avito.utils.gradle.envArgs
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
        val endpoints: List<String>? = project.properties["avito.elastic.endpoints"]?.toString()?.split("|")
        val indexPattern: String? = project.properties["avito.elastic.indexpattern"]?.toString()

        val elasticLogger = if (endpoints != null && indexPattern != null) {
            project.logger.info(
                "ElasticLog: initialized with avito.elastic.endpoints=$endpoints " +
                    "avito.elastic.indexpattern=$indexPattern"
            )

            ElasticLog(
                endpoints = endpoints,
                indexPattern = indexPattern,
                buildId = project.envArgs.build.id.toString(),
                verboseHttpLog = null, // don't enable on production, produces huge logs
                onError = { msg, e -> project.logger.error(msg, e) }
            )
        } else {
            project.logger.info(
                "ElasticLog: set 'avito.elastic.endpoints'(| separated urls) " +
                    "and 'avito.elastic.indexpattern' properties to initialize"
            )
            null
        }

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
    elasticLogger: ElasticLog?
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

private fun elasticHandler(elasticLog: ElasticLog, tag: String, level: String): CILoggingHandler {
    return CILoggingHandlerImplementation(
        destination = ElasticDestination(
            elasticLog = elasticLog,
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
