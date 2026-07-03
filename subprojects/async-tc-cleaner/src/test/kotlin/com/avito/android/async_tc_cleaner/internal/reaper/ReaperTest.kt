package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import com.avito.android.async_tc_cleaner.internal.config.Config
import com.avito.android.async_tc_cleaner.internal.observability.ReapObserver
import com.avito.android.async_tc_cleaner.internal.observability.ReapSweep
import com.avito.logger.PrintlnLoggerFactory
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

class ReaperTest {

    private val logger = PrintlnLoggerFactory.create("test")

    private class Recorder : ReapObserver {
        val reaped = mutableListOf<String>()
        val errors = mutableListOf<String>()
        val sweeps = mutableListOf<ReapSweep>()
        override fun onReaped(entry: String, bytesFreed: Long, entriesDeleted: Long, durationMs: Long) {
            reaped += entry
        }

        override fun onError(entry: String, error: Throwable) {
            errors += entry
        }

        override fun onSweep(summary: ReapSweep) {
            sweeps += summary
        }
    }

    private fun config(workDir: Path, enabled: Boolean = true) = Config(
        reaperEnabledToggleFile = enabledToggleFile(enabled),
        workDir = workDir,
        oldDirName = ".old",
        stagingDirName = ".reaper-staging",
        pollInterval = 1.seconds,
        shutdownTimeout = 25.seconds,
        rmzPath = "/usr/local/bin/rmz",
        node = "test-node",
        pod = "test-pod",
        statsd = null,
        elastic = null,
    )

    private fun enabledToggleFile(enabled: Boolean): Path =
        Files.createTempFile("reaper-enabled", "").also { Files.writeString(it, enabled.toString()) }

    private fun reaper(
        cfg: Config,
        observer: ReapObserver,
        deleter: TreeDeleter = JvmTreeDeleter(),
    ) = Reaper(
        config = cfg,
        observer = observer,
        logger = logger,
        deleter = deleter,
    )

    @Test
    fun `claims and deletes an old-dir entry while leaving old and live checkout intact`(
        @TempDir work: Path,
    ) = runTest {
        val dead = Files.createDirectories(work.resolve(".old/deadcheckout"))
        Files.write(dead.resolve("f"), ByteArray(123))
        val live = Files.createDirectories(work.resolve("livecheckout"))
        Files.write(live.resolve("src"), ByteArray(10))

        val recorder = Recorder()
        reaper(config(work), recorder).runOnce()

        assertFalse(Files.exists(work.resolve(".old/deadcheckout")), "dead entry must be reaped")
        assertTrue(Files.exists(work.resolve(".old")), ".old itself must remain")
        assertTrue(Files.exists(live.resolve("src")), "live checkout must be untouched")
        assertEquals(1, recorder.reaped.size)
        assertTrue(stagingEmpty(work), "staging must be drained")
        val sweep = recorder.sweeps.single()
        assertEquals(1, sweep.claimed)
        assertTrue(sweep.oldDirPresent)
        assertEquals(1, sweep.deleteAttempted)
        assertEquals(1, sweep.reaped)
        assertEquals(123L, sweep.bytesFreed)
        assertEquals(0, sweep.incomplete)
        assertEquals(0, sweep.failed)
    }

    @Test
    fun `does not sweep while the toggle is disabled`(@TempDir work: Path) = runTest {
        Files.createDirectories(work.resolve(".old/dead"))
        val recorder = Recorder()

        reaper(config(work, enabled = false), recorder).runOnce()

        assertTrue(Files.exists(work.resolve(".old/dead")), "nothing claimed while disabled")
        assertTrue(recorder.sweeps.isEmpty(), "no sweep recorded while disabled")
        assertFalse(Files.exists(work.resolve(".reaper-staging")), "staging not created while disabled")
    }

    @Test
    fun `reports old dir absence as a successful empty sweep`(@TempDir work: Path) = runTest {
        val recorder = Recorder()

        reaper(config(work), recorder).runOnce()

        val sweep = recorder.sweeps.single()
        assertFalse(sweep.oldDirPresent)
        assertEquals(0, sweep.claimed)
        assertEquals(0, sweep.failed)
    }

    @Test
    fun `reports delete exception as reap error and failed sweep entry`(@TempDir work: Path) = runTest {
        Files.createDirectories(work.resolve(".old/dead"))
        val recorder = Recorder()
        val failingDelete = object : TreeDeleter {
            override suspend fun delete(root: Path): Result<DeletionOutcome> = error("failed")
        }

        reaper(config(work), recorder, failingDelete).runOnce()

        assertEquals(listOf("dead"), recorder.errors)
        val sweep = recorder.sweeps.single()
        assertEquals(1, sweep.claimed)
        assertEquals(1, sweep.deleteAttempted)
        assertEquals(1, sweep.failed)
    }

    @Test
    fun `reports incomplete delete outcome as reap error`(@TempDir work: Path) = runTest {
        Files.createDirectories(work.resolve(".old/dead"))
        val recorder = Recorder()
        val incompleteDelete = object : TreeDeleter {
            override suspend fun delete(root: Path): Result<DeletionOutcome> = Result.Success(
                DeletionOutcome(bytesFreed = 0, entriesDeleted = 0, failures = 1, failureSamples = listOf("sample"))
            )
        }

        reaper(config(work), recorder, incompleteDelete).runOnce()

        assertEquals(listOf("dead"), recorder.errors)
        val sweep = recorder.sweeps.single()
        assertEquals(1, sweep.incomplete)
        assertEquals(1L, sweep.unlinkFailures)
    }

    @Test
    fun `skips the sweep without following a symlinked staging dir`(@TempDir work: Path) = runTest {
        val sentinel = Files.createDirectories(work.resolve("sentinel"))
        val victim = Files.write(sentinel.resolve("victim"), ByteArray(10))
        Files.createDirectories(work.resolve(".old/dead"))
        Files.createSymbolicLink(work.resolve(".reaper-staging"), sentinel)

        val recorder = Recorder()
        reaper(config(work), recorder).runOnce()

        assertTrue(Files.exists(victim), "symlink target contents must survive")
        assertTrue(Files.exists(work.resolve(".old/dead")), "nothing is claimed when staging is misconfigured")
        assertTrue(recorder.reaped.isEmpty(), "no reaping when staging is misconfigured")
        assertTrue(recorder.sweeps.isEmpty(), "the sweep is skipped, not run")
    }

    @Test
    fun `resumes orphaned staging entries from a crashed run`(@TempDir work: Path) = runTest {
        Files.createDirectories(work.resolve(".reaper-staging/orphan.deadbeef/inner"))

        val recorder = Recorder()
        reaper(config(work), recorder).runOnce()

        assertFalse(Files.exists(work.resolve(".reaper-staging/orphan.deadbeef")), "orphan must be deleted")
        assertEquals(1, recorder.reaped.size)
        assertEquals("orphan", recorder.reaped.single())
    }

    private fun stagingEmpty(work: Path): Boolean =
        Files.newDirectoryStream(work.resolve(".reaper-staging")).use { !it.iterator().hasNext() }
}
