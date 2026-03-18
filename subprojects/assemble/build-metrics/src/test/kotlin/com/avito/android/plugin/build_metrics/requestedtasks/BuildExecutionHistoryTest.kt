package com.avito.android.plugin.build_metrics.requestedtasks

import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.BuildExecutionHistory
import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.BuildExecutionState
import com.avito.logger.PrintlnLoggerFactory
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class BuildExecutionHistoryTest {

    @Test
    fun `returns null - no history file exists`(@TempDir tempDir: File) {
        val historyFile = File(tempDir, "nonexistent.properties")
        val history = BuildExecutionHistory(historyFile, PrintlnLoggerFactory)

        assertThat(history.readPreviousState()).isNull()
    }

    @Test
    fun `reads previously written state`(@TempDir tempDir: File) {
        val historyFile = File(tempDir, "execution-history.properties")
        val history = BuildExecutionHistory(historyFile, PrintlnLoggerFactory)

        val state = BuildExecutionState(commit = "abc123def456")
        history.writeCurrentState(state)

        val readState = history.readPreviousState()
        assertThat(readState).isNotNull()
        assertThat(readState?.commit).isEqualTo("abc123def456")
    }

    @Test
    fun `overwrites previous state`(@TempDir tempDir: File) {
        val historyFile = File(tempDir, "execution-history.properties")
        val history = BuildExecutionHistory(historyFile, PrintlnLoggerFactory)

        history.writeCurrentState(BuildExecutionState(commit = "111"))
        history.writeCurrentState(BuildExecutionState(commit = "222"))

        val readState = history.readPreviousState()
        assertThat(readState).isNotNull()
        assertThat(readState?.commit).isEqualTo("222")
    }

    @Test
    fun `creates parent directories if needed`(@TempDir tempDir: File) {
        val historyFile = File(tempDir, "nested/dir/execution-history.properties")
        val history = BuildExecutionHistory(historyFile, PrintlnLoggerFactory)

        history.writeCurrentState(BuildExecutionState(commit = "abc"))

        assertThat(historyFile.exists()).isTrue()
        assertThat(history.readPreviousState()).isNotNull()
    }

    @Test
    fun `returns null - corrupted file`(@TempDir tempDir: File) {
        val historyFile = File(tempDir, "execution-history.properties")
        historyFile.writeText("this is not a valid properties file \u0000\u0000\u0000")

        val history = BuildExecutionHistory(historyFile, PrintlnLoggerFactory)
        // Properties can parse almost anything, so test with missing keys
        val readState = history.readPreviousState()
        assertThat(readState).isNull()
    }

    @Test
    fun `returns null - file has no commit`(@TempDir tempDir: File) {
        val historyFile = File(tempDir, "execution-history.properties")
        historyFile.writeText("something=develop\n")

        val history = BuildExecutionHistory(historyFile, PrintlnLoggerFactory)
        assertThat(history.readPreviousState()).isNull()
    }
}
