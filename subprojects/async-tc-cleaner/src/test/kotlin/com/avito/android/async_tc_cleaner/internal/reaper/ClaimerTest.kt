package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.logger.PrintlnLoggerFactory
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant

class ClaimerTest {

    private val logger = PrintlnLoggerFactory.create("test")

    private fun claimer(oldDir: Path, stagingDir: Path) =
        Claimer(oldDir = oldDir, stagingDir = stagingDir, logger = logger)

    @Test
    fun `reports oldDirPresent false when the old dir is absent`(@TempDir work: Path) = runTest {
        val staging = Files.createDirectories(work.resolve(".reaper-staging"))

        val stats = claimer(work.resolve(".old"), staging).claimEligible()

        assertEquals(ClaimStats(oldDirPresent = false), stats)
    }

    @Test
    fun `treats a non-directory old path as absent`(@TempDir work: Path) = runTest {
        val staging = Files.createDirectories(work.resolve(".reaper-staging"))
        val oldAsFile = Files.write(work.resolve(".old"), ByteArray(3))

        val stats = claimer(oldAsFile, staging).claimEligible()

        assertFalse(stats.oldDirPresent)
        assertEquals(0, stats.claimed)
    }

    @Test
    fun `claims an entry into staging under a uuid-suffixed name`(@TempDir work: Path) = runTest {
        val old = Files.createDirectories(work.resolve(".old"))
        val staging = Files.createDirectories(work.resolve(".reaper-staging"))
        val entry = Files.createDirectories(old.resolve("deadcheckout"))
        Files.write(entry.resolve("f"), ByteArray(10))

        val stats = claimer(old, staging).claimEligible()

        assertEquals(1, stats.claimed)
        assertTrue(stats.oldDirPresent)
        assertFalse(Files.exists(old.resolve("deadcheckout")), "claimed entry must leave the old dir")
        val staged = Files.newDirectoryStream(staging).use { it.toList() }
        assertEquals(1, staged.size, "exactly one entry lands in staging")
        assertTrue(
            staged.single().fileName.toString().startsWith("deadcheckout."),
            "staged name keeps the original plus a uuid suffix",
        )
    }

    @Test
    fun `claims every entry regardless of its mtime`(@TempDir work: Path) = runTest {
        val old = Files.createDirectories(work.resolve(".old"))
        val staging = Files.createDirectories(work.resolve(".reaper-staging"))
        Files.setLastModifiedTime(
            Files.createDirectories(old.resolve("aged")),
            FileTime.from(Instant.now().minusSeconds(3600)),
        )
        Files.createDirectories(old.resolve("fresh"))

        val stats = claimer(old, staging).claimEligible()

        assertEquals(2, stats.claimed)
        assertTrue(stats.oldDirPresent)
        assertEquals(2, childCount(staging))
        assertEquals(0, childCount(old), "every entry leaves the old dir")
    }

    private fun childCount(dir: Path): Int = Files.newDirectoryStream(dir).use { stream ->
        return stream.count()
    }
}
