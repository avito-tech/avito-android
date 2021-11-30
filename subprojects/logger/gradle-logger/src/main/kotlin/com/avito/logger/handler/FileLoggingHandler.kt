package com.avito.logger.handler

import com.avito.logger.LogLevel
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path

internal class FileLoggingHandler(
    private val messagePrefix: String,
    acceptedLogLevel: LogLevel,
    logFile: Path
) : LogLevelLoggingHandler(acceptedLogLevel) {

    private val fileWriter by lazy {
        if (!Files.exists(logFile)) { Files.createFile(logFile) }
        Files.newBufferedWriter(logFile)
    }
    private val stackTraceWriter by lazy { PrintWriter(fileWriter) }

    override fun handleIfAcceptLogLevel(level: LogLevel, message: String, error: Throwable?) {
        fileWriter.write("$messagePrefix [$level] $message")
        fileWriter.newLine()
        if (error != null) {
            val errorMessage = error.message
            if (errorMessage != null) {
                fileWriter.write(errorMessage)
                fileWriter.newLine()
            }
            error.printStackTrace(stackTraceWriter)
            fileWriter.newLine()
        }
        // TODO remove that flush. Could be done by adding `close` fun. Where we will close fileWriter
        fileWriter.flush()
    }
}
