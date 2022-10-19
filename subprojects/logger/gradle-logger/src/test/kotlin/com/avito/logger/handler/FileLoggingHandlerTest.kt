package com.avito.logger.handler

import com.avito.logger.LogLevel
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future

class FileLoggingHandlerTest {

    @Test
    fun `when 10 threads write to one log file concurrenty - we have all log lines`(@TempDir file: File) {
        val logFile = File(file, "logs.txt")
        val pool = Executors.newFixedThreadPool(10)
        val jobs = mutableListOf<Future<*>>()
        val expectedLinesCount = 100
        repeat(10) {
            val logFuture = pool.submit {
                val handler = FileLoggingHandler("test", acceptedLogLevel = LogLevel.DEBUG, logFile.toPath())
                repeat(10) {
                    handler.write(LogLevel.DEBUG, Thread.currentThread().name, null)
                }
            }
            jobs.add(logFuture)
        }
        jobs.forEach { it.get() }
        Truth
            .assertWithMessage("Assert lines count in logs file")
            .that(logFile.readLines().count())
            .isEqualTo(expectedLinesCount)
        pool.shutdownNow()
    }
}
