package com.avito.logger.handler

import com.avito.logger.LogLevel
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.appendText

internal class FileLoggingHandler(
    private val messagePrefix: String,
    acceptedLogLevel: LogLevel,
    logFile: Path,
) : LogLevelLoggingHandler(acceptedLogLevel) {

    private val logFile by lazy(lock) {
        if (!Files.exists(logFile)) {
            Files.createFile(logFile)
        }
        logFile
    }

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        val logString = buildString {
            appendLine("$messagePrefix [$level] $message")
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
    }
}
