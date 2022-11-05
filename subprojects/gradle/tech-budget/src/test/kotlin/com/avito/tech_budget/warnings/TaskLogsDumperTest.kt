package com.avito.tech_budget.warnings

import com.avito.android.tech_budget.internal.warnings.log.LogWriter
import com.avito.android.tech_budget.internal.warnings.log.TaskLogsDumper
import com.google.common.truth.Truth.assertThat
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.LogLevel.DEBUG
import org.gradle.api.logging.LogLevel.WARN
import org.gradle.internal.logging.events.LogEvent
import org.junit.jupiter.api.Test

class TaskLogsDumperTest {

    private val logSaver = FakeLogWriter()

    @Test
    fun `logs warning - message saved`() {
        val targetLogLevel = WARN

        val dumper = createDumper(targetLogLevel)
        val message = "Something wrong"

        dumper.onOutput(createLogEvent(targetLogLevel, message))
    }

    @Test
    fun `logs several warnings - all saved`() {
        val targetLogLevel = WARN
        val dumper = createDumper(targetLogLevel)

        dumper.onOutput(createLogEvent(targetLogLevel, "Something wrong"))
        dumper.onOutput(createLogEvent(targetLogLevel, "Another thing is wrong"))

        assertThat(logSaver.logMessages).contains("Something wrong")
        assertThat(logSaver.logMessages).contains("Another thing is wrong")
    }

    @Test
    fun `logs debug - saves nothing`() {
        val dumper = createDumper(targetLogLevel = WARN)
        val message = "Something wrong"

        dumper.onOutput(createLogEvent(logLevel = DEBUG, message))

        assertThat(logSaver.logMessages).isEmpty()
    }

    private fun createDumper(
        targetLogLevel: LogLevel
    ): TaskLogsDumper {
        return TaskLogsDumper(
            targetLogLevel = targetLogLevel,
            logWriter = logSaver
        )
    }

    private class FakeLogWriter : LogWriter {
        private val savedMessages = mutableListOf<String>()
        val logMessages: List<String> = savedMessages

        override fun save(logMessage: String) {
            savedMessages.add(logMessage)
        }
    }

    private fun createLogEvent(
        logLevel: LogLevel,
        message: String
    ) = LogEvent(
        System.currentTimeMillis(),
        "test",
        logLevel,
        message,
        null,
        null
    )
}
