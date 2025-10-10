package com.avito.logger.handler

import com.avito.logger.LogLevel
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import kotlin.io.path.appendText

internal class FileLoggingHandler(
    private val messagePrefix: String,
    acceptedLogLevel: LogLevel,
    logFile: Path,
    private val clock: Clock = Clock.systemDefaultZone(),
) : LogLevelLoggingHandler(acceptedLogLevel) {

    private val logFile by lazy(lock) {
        if (!Files.exists(logFile)) {
            Files.createFile(logFile)
        }
        logFile
    }

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        val logString = buildString {
            val timestamp = ZonedDateTime.now(clock).format(timestampFormatter)
            appendLine("$timestamp $level $messagePrefix $message")
            if (error != null) {
                val errorMessage = error.message
                if (errorMessage != null) {
                    appendLine(errorMessage)
                }
                val sw = StringWriter()
                error.printStackTrace(PrintWriter(sw))
                append(sw.toString())
            }
        }
        logFile.appendText(logString)
    }

    companion object {
        private val lock = Any()
        private val timestampFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .appendOffset("+H", "Z")
            .toFormatter()
    }
}
