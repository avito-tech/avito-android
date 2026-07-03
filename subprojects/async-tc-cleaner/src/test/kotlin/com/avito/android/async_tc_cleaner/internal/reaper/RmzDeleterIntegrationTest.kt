package com.avito.android.async_tc_cleaner.internal.reaper

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class RmzDeleterIntegrationTest {

    @Test
    fun `real rmz deletes the staged tree, spares symlink targets, and reports accurate counts`(
        @TempDir tmp: Path,
    ) = runTest {
        val rmz = resolveRmz()
        assumeTrue(rmz != null, "rmz binary not available; skipping real-binary integration test")

        val staging = Files.createDirectories(tmp.resolve(".reaper-staging"))
        val sentinelDir = Files.createDirectories(tmp.resolve("sentinel"))
        val sentinelFile = Files.write(sentinelDir.resolve("keep"), ByteArray(10))
        val extFile = Files.write(tmp.resolve("ext"), ByteArray(5))

        val root = Files.createDirectories(staging.resolve("dead/sub"))
        Files.write(staging.resolve("dead/a"), ByteArray(111))
        Files.write(root.resolve("b"), ByteArray(222))
        Files.createSymbolicLink(root.resolve("dirlink"), sentinelDir)
        Files.createSymbolicLink(root.resolve("filelink"), extFile)
        val stagedRoot = staging.resolve("dead")
        val expected = TreeMeasurerImpl.measure(stagedRoot)

        val outcome = RmzDeleter(rmzPath = rmz!!).delete(stagedRoot).getOrThrow()

        assertFalse(Files.exists(stagedRoot), "rmz removed the staged tree")
        assertTrue(Files.exists(sentinelFile), "symlinked dir target survives")
        assertTrue(Files.exists(extFile), "symlinked file target survives")
        assertEquals(0, outcome.failures)
        assertEquals(expected.bytes, outcome.bytesFreed)
        assertEquals(expected.entries, outcome.entriesDeleted)
    }

    private fun resolveRmz(): String? = listOfNotNull(
        System.getenv("RMZ_PATH"),
        "/usr/local/bin/rmz",
        System.getProperty("user.home")?.let { "$it/.cargo/bin/rmz" },
    ).firstOrNull { File(it).canExecute() }
}
