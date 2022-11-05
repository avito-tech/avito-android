package com.avito.tech_budget.warnings

import com.avito.android.tech_budget.internal.warnings.log.FileLogReader
import com.avito.android.tech_budget.internal.warnings.log.FileLogWriter
import com.avito.android.tech_budget.internal.warnings.log.FileLogWriter.Companion.DEFAULT_SEPARATOR
import com.avito.android.tech_budget.internal.warnings.log.LogFileProjectProvider
import com.avito.android.tech_budget.internal.warnings.log.ProjectInfo
import com.avito.android.tech_budget.internal.warnings.log.converter.ProjectInfoConverter
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future

class LogStorageTest {

    @Test
    fun `when read empty output - empty list returned`(@TempDir output: File) {
        val logEntries = createLogReader(output).getAll()
        assertThat(logEntries).isEmpty()
    }

    @Test
    fun `when read output without project file - error returned`(@TempDir output: File) {
        val handler = createLogReader(output)

        val moduleDir = File(output, "app")
        createFile(moduleDir)

        val error = assertThrows<IllegalStateException> {
            handler.getAll()
        }

        assertThat(error)
            .hasMessageThat()
            .contains("File `.project` must present in project directory with warnings report")
    }

    @Test
    fun `when read output with empty project file - project path error returned`(@TempDir output: File) {
        val handler = createLogReader(output)

        val moduleDir = File(output, "app")

        createFile(moduleDir)
        createProjectFile(moduleDir, projectPath = "", owners = emptyList())

        val error = assertThrows<IllegalStateException> {
            handler.getAll()
        }

        assertThat(error)
            .hasMessageThat()
            .contains("Project path can't be blank")
    }

    @Test
    fun `when read output with correct files - entries collected`(@TempDir output: File) {
        val handler = createLogReader(output)

        val projectPath = ":app"
        val owners = listOf("Speed", "Messenger")
        val moduleDir = File(output, "app")

        createFile(moduleDir)
        createProjectFile(moduleDir, projectPath, owners)

        val logEntries = handler.getAll()

        assertThat(logEntries).hasSize(1)

        val savedEntry = logEntries.first()
        assertThat(savedEntry.projectInfo.owners).isEqualTo(owners)
        assertThat(savedEntry.projectInfo.path).isEqualTo(projectPath)
    }

    @Test
    fun `when 10 threads write to one log file concurrently - we have collected all entries`(@TempDir output: File) {
        val pool = Executors.newFixedThreadPool(10)
        val jobs = mutableListOf<Future<*>>()
        val expectedEntriesCount = 100
        repeat(10) { i ->
            val logFuture = pool.submit {
                val saver = createLogWriter(output, i)
                repeat(10) {
                    saver.save(Thread.currentThread().name)
                }
            }
            jobs.add(logFuture)
        }
        jobs.forEach { it.get() }

        val logEntries = createLogReader(output).getAll()

        assertThat(logEntries)
            .hasSize(expectedEntriesCount)
        pool.shutdownNow()
    }

    private companion object {

        fun createLogReader(output: File) =
            FileLogReader(output, DEFAULT_SEPARATOR, createProjectInfoConverter())

        fun createLogWriter(output: File, number: Int) =
            FileLogWriter(
                fileProvider = LogFileProjectProvider(
                    rootOutputDir = output,
                    projectInfo = ProjectInfo(":app", setOf()),
                    taskName = "compileKotlin_$number",
                    projectInfoConverter = createProjectInfoConverter()
                ),
                separator = DEFAULT_SEPARATOR
            )

        fun createFile(
            directory: File,
            name: String = "compileRelease.log",
            content: String = "Logging something$DEFAULT_SEPARATOR"
        ) =
            File(directory, name).apply {
                parentFile.mkdirs()
                createNewFile()
                appendText(content)
            }

        fun createProjectFile(
            directory: File,
            projectPath: String = ":app",
            owners: List<String>,
        ) = createFile(
            directory,
            name = ".project",
            content = createProjectInfoConverter().convertToString(ProjectInfo(projectPath, owners))
        )

        fun createProjectInfoConverter() = ProjectInfoConverter.default()
    }
}
