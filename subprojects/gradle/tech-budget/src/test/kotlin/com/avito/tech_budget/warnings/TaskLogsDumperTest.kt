package com.avito.tech_budget.warnings

import com.avito.android.tech_budget.internal.warnings.log.LogWriter
import com.avito.android.tech_budget.internal.warnings.log.TaskLogsDumper
import com.avito.android.tech_budget.internal.warnings.task.TaskBuildOperationIdProvider
import com.google.common.truth.Truth.assertThat
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.LogLevel.DEBUG
import org.gradle.api.logging.LogLevel.WARN
import org.gradle.internal.logging.events.LogEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.util.Path
import org.junit.jupiter.api.Test

class TaskLogsDumperTest {

    private val logSaver = FakeLogWriter()
    private val buildOperationId = OperationIdentifier(123)

    @Test
    fun `logs warning - message saved`() {
        val targetLogLevel = WARN

        val dumper = createDumper(targetLogLevel)
        val message = "Something wrong"

        dumper.onOutput(createLogEvent(targetLogLevel, message))
        assertThat(logSaver.logMessages).contains("Something wrong")
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

    @Test
    fun `logs warning but from different build operation - saves nothing`() {
        val targetLogLevel = WARN

        val dumper = createDumper(targetLogLevel)
        val message = "Something wrong"
        val logEvent = createLogEvent(targetLogLevel, message, OperationIdentifier(456))

        dumper.onOutput(logEvent)
        assertThat(logSaver.logMessages).isEmpty()
    }

    private fun createDumper(
        targetLogLevel: LogLevel
    ): TaskLogsDumper {
        return TaskLogsDumper(
            targetLogLevel = targetLogLevel,
            logWriter = logSaver,
            taskPath = Path.path("auth:compile"),
            taskBuildOperationIdProvider = FakeBuildOperationIdProvider(),
        )
    }

    private class FakeLogWriter : LogWriter {
        private val savedMessages = mutableListOf<String>()
        val logMessages: List<String> = savedMessages

        override fun save(logMessage: String) {
            savedMessages.add(logMessage)
        }
    }

    private inner class FakeBuildOperationIdProvider : TaskBuildOperationIdProvider {
        override fun getBuildOperationId(taskPath: Path): OperationIdentifier {
            return buildOperationId
        }
    }

    private fun createLogEvent(
        logLevel: LogLevel,
        message: String,
        buildOperationId: OperationIdentifier = this.buildOperationId
    ) = LogEvent(
        System.currentTimeMillis(),
        "test",
        logLevel,
        message,
        null,
        buildOperationId
    )
}
