package com.avito.tech_budget.warnings

import com.avito.android.tech_budget.internal.warnings.log.FileLogStorage
import com.avito.android.tech_budget.internal.warnings.log.FileLogStorage.Companion.DEFAULT_SEPARATOR
import com.avito.android.tech_budget.internal.warnings.log.LogEntry
import com.avito.android.tech_budget.internal.warnings.log.LogStorage
import com.avito.android.tech_budget.internal.warnings.log.ProjectInfo
import com.avito.android.tech_budget.internal.warnings.log.converter.JsonProjectInfoConverter
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
        val handler = createLogStorage(output)

        val logEntries = handler.getAll()

        assertThat(logEntries).isEmpty()
    }

    @Test
    fun `when read output without project file - error returned`(@TempDir output: File) {
        val handler = createLogStorage(output)

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
        val handler = createLogStorage(output)

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
        val handler = createLogStorage(output)

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
        repeat(10) {
            val logFuture = pool.submit {
                val handler = createLogStorage(output)
                repeat(10) {
                    handler.save(
                        LogEntry(
                            ProjectInfo(":app", setOf()),
                            "compileKotlin",
                            Thread.currentThread().name
                        )
                    )
                }
            }
            jobs.add(logFuture)
        }
        jobs.forEach { it.get() }

        val logEntries = createLogStorage(output).getAll()

        assertThat(logEntries)
            .hasSize(expectedEntriesCount)
        pool.shutdownNow()
    }

    private companion object {

        fun createLogStorage(output: File): LogStorage {
            return FileLogStorage(
                output,
                projectInfoConverter = JsonProjectInfoConverter()
            )
        }

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
            content = JsonProjectInfoConverter().convertToString(ProjectInfo(projectPath, owners))
        )
    }
}
