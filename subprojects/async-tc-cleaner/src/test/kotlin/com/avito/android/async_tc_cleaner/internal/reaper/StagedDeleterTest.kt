package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.async_tc_cleaner.internal.observability.ReapObserver
import com.avito.logger.PrintlnLoggerFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

class StagedDeleterTest {

    private val logger = PrintlnLoggerFactory.create("test")
    private val jvm = SinglePassTreeDeleter()

    private class Recorder : ReapObserver {
        val reaped = mutableListOf<String>()
        val errors = mutableListOf<String>()
        override fun onReaped(entry: String, bytesFreed: Long, entriesDeleted: Long, durationMs: Long) {
            reaped += entry
        }

        override fun onError(entryName: String, error: Throwable) {
            errors += entryName
        }
    }

    private suspend fun deleteAll(
        stagingDir: Path,
        observer: ReapObserver,
        treeDeleter: TreeDeleter = jvm,
    ): DeleteStats = stagingDir.directoryStream()
        .getOrElse { e -> error(e) }
        .use { stream ->
            StagedDeleter(stagingDir, treeDeleter, observer, logger).deleteAll(stream)
        }

    private fun staged(stagingDir: Path, name: String, fileBytes: Int = 0): Path {
        val dir = Files.createDirectories(stagingDir.resolve(name))
        if (fileBytes > 0) Files.write(dir.resolve("f"), ByteArray(fileBytes))
        return dir
    }

    @Test
    fun `empty staging yields zero stats and no observer calls`(@TempDir staging: Path) = runTest {
        val recorder = Recorder()

        val stats = deleteAll(staging, recorder)

        assertEquals(DeleteStats(), stats)
        assertTrue(recorder.reaped.isEmpty() && recorder.errors.isEmpty())
    }

    @Test
    fun `reaps a staged tree and reports its bytes, entries and name`(@TempDir staging: Path) = runTest {
        val dir = staged(staging, "foo.${UUID.randomUUID()}")
        Files.write(dir.resolve("a"), ByteArray(100))
        Files.write(dir.resolve("b"), ByteArray(50))
        val recorder = Recorder()

        val stats = deleteAll(staging, recorder)

        assertEquals(1, stats.reaped)
        assertEquals(1, stats.attempted)
        assertEquals(150L, stats.bytesFreed)
        assertEquals(3L, stats.entriesDeleted, "two files plus the dir itself")
        assertEquals(listOf("foo"), recorder.reaped)
        assertEquals(0, childCount(staging), "staging is drained")
    }

    @Test
    fun `recovers the original dotted name from the staged entry`(@TempDir staging: Path) = runTest {
        staged(staging, "my.dotted.name.${UUID.randomUUID()}", fileBytes = 5)
        val recorder = Recorder()

        deleteAll(staging, recorder)

        assertEquals(listOf("my.dotted.name"), recorder.reaped, "only the uuid suffix is stripped")
    }

    @Test
    fun `reports a failed delete result as a failed reap`(@TempDir staging: Path) = runTest {
        staged(staging, "dead.${UUID.randomUUID()}", fileBytes = 5)
        val recorder = Recorder()
        val failing = object : TreeDeleter {
            override suspend fun delete(stagedDir: StagedDir): DeletionOutcome =
                throw IOException("boom")
        }

        val stats = deleteAll(staging, recorder, failing)

        assertEquals(1, stats.attempted)
        assertEquals(1, stats.failed)
        assertEquals(listOf("dead"), recorder.errors)
        assertTrue(stats.failureSamples.isNotEmpty())
    }

    @Test
    fun `reports unlink failures from the deleter as incomplete`(@TempDir staging: Path) = runTest {
        staged(staging, "partial.${UUID.randomUUID()}", fileBytes = 5)
        val recorder = Recorder()
        val reportsFailure = object : TreeDeleter {
            override suspend fun delete(stagedDir: StagedDir): DeletionOutcome {
                jvm.delete(stagedDir)
                return DeletionOutcome(
                    bytesFreed = 5,
                    entriesDeleted = 2,
                    failures = 1,
                    failureSamples = listOf("unlink x"),
                )
            }
        }

        val stats = deleteAll(staging, recorder, reportsFailure)

        assertEquals(1, stats.incomplete)
        assertEquals(1L, stats.failures)
        assertEquals(5L, stats.bytesFreed)
        assertEquals(listOf("partial"), recorder.errors)
        assertFalse(stats.failureSamples.any { it.contains("staged root still present") })
    }

    @Test
    fun `reports a surviving staged root as incomplete`(@TempDir staging: Path) = runTest {
        staged(staging, "stuck.${UUID.randomUUID()}", fileBytes = 5)
        val recorder = Recorder()
        val noop = object : TreeDeleter {
            override suspend fun delete(stagedDir: StagedDir): DeletionOutcome =
                DeletionOutcome.EMPTY
        }

        val stats = deleteAll(staging, recorder, noop)

        assertEquals(1, stats.incomplete)
        assertEquals(0L, stats.failures)
        assertEquals(listOf("stuck"), recorder.errors)
        assertTrue(stats.failureSamples.any { it.contains("staged root still present") })
    }

    @Test
    fun `propagates cancellation without recording a reap error`(@TempDir staging: Path) {
        staged(staging, "x.${UUID.randomUUID()}", fileBytes = 5)
        val recorder = Recorder()
        val cancelling = object : TreeDeleter {
            override suspend fun delete(stagedDir: StagedDir): DeletionOutcome =
                throw CancellationException("stop")
        }

        assertThrows<CancellationException> {
            runBlocking { deleteAll(staging, recorder, cancelling) }
        }
        assertTrue(recorder.errors.isEmpty(), "cancellation is not a delete failure")
    }

    private fun childCount(dir: Path): Int = Files.newDirectoryStream(dir).use { stream ->
        return stream.count()
    }
}
