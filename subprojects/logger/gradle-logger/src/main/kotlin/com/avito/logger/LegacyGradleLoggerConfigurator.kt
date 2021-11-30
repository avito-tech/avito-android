package com.avito.logger

import org.gradle.api.Project
import org.gradle.api.logging.configuration.ShowStacktrace
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Deprecated("Remove after 2021.38 release")
internal class LegacyGradleLoggerConfigurator(
    private val project: Project
) {

    fun configure(parameters: LoggerService.Params) {
        with(parameters) {
            val verbosity = getVerbosity() ?: LogLevel.INFO
            printlnHandler.set(GradleLoggerExtension.PrintlnMode(verbosity, doPrintStackTrace()))
            fileHandler.set(LogLevel.INFO)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
                .withZone(ZoneId.from(ZoneOffset.UTC))
            val instant = Instant.now()
            fileHandlerRootDir.set(project.layout.buildDirectory.dir("logs").map { it.dir(formatter.format(instant)) })
            finalized.set(true)
        }
    }

    private fun doPrintStackTrace(): Boolean {
        val showStacktrace = project.gradle.startParameter.showStacktrace
        return showStacktrace == ShowStacktrace.ALWAYS || showStacktrace == ShowStacktrace.ALWAYS_FULL
    }

    private fun getVerbosity(): LogLevel? {
        return project.providers
            .gradleProperty("avito.logging.verbosity")
            .forUseAtConfigurationTime()
            .map { value ->
                try {
                    LogLevel.valueOf(value.uppercase(Locale.getDefault()))
                } catch (e: Throwable) {
                    throw IllegalArgumentException(
                        "`avito.logging.verbosity` should be one of: " +
                            "${LogLevel.values().map { it.name }} but was $value"
                    )
                }
            }
            .orNull
    }
}
