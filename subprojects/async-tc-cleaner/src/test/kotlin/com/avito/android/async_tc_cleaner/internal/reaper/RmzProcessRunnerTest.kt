package com.avito.android.async_tc_cleaner.internal.reaper

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.io.File

class RmzProcessRunnerTest {

    @Test
    fun `bounds stderr when a process floods it with one error line per failure`() = runTest {
        assumeShell()
        val flood = "i=0; while [ \$i -lt 5000 ]; do echo \"unlink failed \$i\" >&2; i=\$((i+1)); done; exit 1"

        val result = RmzProcessRunnerImpl.run(listOf("sh", "-c", flood)).getOrThrow()

        assertEquals(1, result.exitCode)
        assertTrue(result.stderr.size in 1..200, "stderr must stay bounded, was ${result.stderr.size}")
    }

    @Test
    fun `reports the exit code and drops blank stderr lines`() = runTest {
        assumeShell()

        val result = RmzProcessRunnerImpl.run(listOf("sh", "-c", "echo real >&2; echo >&2; exit 0")).getOrThrow()

        assertEquals(0, result.exitCode)
        assertEquals(listOf("real"), result.stderr)
    }

    private fun assumeShell() = assumeTrue(File("/bin/sh").canExecute(), "POSIX shell not available")
}
