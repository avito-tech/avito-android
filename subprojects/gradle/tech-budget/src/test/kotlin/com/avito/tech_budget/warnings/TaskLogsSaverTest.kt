package com.avito.tech_budget.warnings

import com.avito.android.tech_budget.internal.warnings.log.FileLogStorage
import com.avito.android.tech_budget.internal.warnings.log.ProjectInfo
import com.avito.android.tech_budget.internal.warnings.log.TaskLogsSaver
import com.avito.android.tech_budget.internal.warnings.log.converter.JsonProjectInfoConverter
import com.google.common.truth.Truth.assertThat
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.LogLevel.DEBUG
import org.gradle.api.logging.LogLevel.WARN
import org.gradle.internal.logging.events.LogEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TaskLogsSaverTest {

    @Test
    fun `logs warning - log file generated`(@TempDir outputDir: File) {
        val targetLogLevel = WARN

        val dumper = createDumper(
            outputDir = outputDir,
            targetLogLevel = targetLogLevel
        )
        val message = "Something wrong"

        dumper.onOutput(createLogEvent(targetLogLevel, message))

        val logFile = File(outputDir, "app/compileKotlin.log")
        assert(logFile.exists())

        assertThat(logFile.readText()).contains(message)
    }

    @Test
    fun `logs several warnings - all saved to file`(@TempDir outputDir: File) {
        val targetLogLevel = WARN

        val dumper = createDumper(
            outputDir = outputDir,
            targetLogLevel = targetLogLevel
        )

        dumper.onOutput(createLogEvent(targetLogLevel, "Something wrong"))
        dumper.onOutput(createLogEvent(targetLogLevel, "Another thing is wrong"))

        val logFile = File(outputDir, "app/compileKotlin.log")
        assert(logFile.exists())

        val logFileText = logFile.readText()
        assertThat(logFileText).contains("Something wrong")
        assertThat(logFileText).contains("Another thing is wrong")
    }

    @Test
    fun `logs warning - project file generated`(@TempDir outputDir: File) {
        val targetLogLevel = WARN

        val dumper = createDumper(
            outputDir = outputDir,
            targetLogLevel = targetLogLevel
        )
        val message = "Something wrong"

        dumper.onOutput(createLogEvent(targetLogLevel, message))

        val projectFile = File(outputDir, "app/.project")
        assertThat(projectFile.exists()).isTrue()

        assertThat(projectFile.readText()).contains(":app")
    }

    @Test
    fun `logs debug - saves nothing`(@TempDir outputDir: File) {
        val dumper = createDumper(
            outputDir = outputDir,
            targetLogLevel = WARN
        )
        val message = "Something wrong"

        dumper.onOutput(createLogEvent(logLevel = DEBUG, message))

        assertThat(outputDir.listFiles()).isEmpty()
    }

    private fun createDumper(
        taskName: String = "compileKotlin",
        projectPath: String = ":app",
        outputDir: File,
        targetLogLevel: LogLevel
    ): TaskLogsSaver {
        return TaskLogsSaver(
            taskName = taskName,
            projectInfo = ProjectInfo(projectPath, owners = listOf()),
            targetLogLevel = targetLogLevel,
            logStorage = FileLogStorage(outputDir, projectInfoConverter = JsonProjectInfoConverter())
        )
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
