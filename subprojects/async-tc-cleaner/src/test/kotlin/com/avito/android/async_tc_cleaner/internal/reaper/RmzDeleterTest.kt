package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class RmzDeleterTest {

    @Test
    fun `exit 0 yields the pre-measured outcome and invokes rmz with -f --`(@TempDir tmp: Path) = runTest {
        val root = Files.createDirectories(tmp.resolve("x"))
        var cmd: List<String>? = null
        val rmz = executable(tmp)
        val deleter = RmzDeleter(
            rmzPath = rmz,
            measurer = { TreeMeasurement(bytes = 123, entries = 7) },
            processRunner = { cmd = it; Result.Success(ProcessResult(0, emptyList())) },
        )

        val outcome = deleter.delete(root).getOrThrow()

        assertEquals(123, outcome.bytesFreed)
        assertEquals(7, outcome.entriesDeleted)
        assertEquals(0, outcome.failures)
        assertEquals(listOf(rmz, "-f", "--", root.toString()), cmd)
    }

    @Test
    fun `non-zero exit but root gone is a benign race counted as success`(@TempDir tmp: Path) = runTest {
        val root = Files.createDirectories(tmp.resolve("x"))
        val deleter = RmzDeleter(
            rmzPath = executable(tmp),
            measurer = { TreeMeasurement(bytes = 10, entries = 3) },
            processRunner = {
                root.toFile().deleteRecursively() // rmz finished the tree despite a mid-walk ENOENT
                Result.Success(ProcessResult(1, listOf("rmz: No such file or directory (os error 2)")))
            },
        )

        val outcome = deleter.delete(root).getOrThrow()

        assertEquals(10, outcome.bytesFreed)
        assertEquals(3, outcome.entriesDeleted)
        assertEquals(0, outcome.failures, "an ENOENT race that left the tree gone is not a failure")
    }

    @Test
    fun `non-zero exit with a surviving remnant credits what is gone and surfaces real errors`(
        @TempDir tmp: Path,
    ) = runTest {
        val root = Files.createDirectories(tmp.resolve("x")) // still present after rmz -> partial failure
        val measures = ArrayDeque(listOf(TreeMeasurement(100, 5), TreeMeasurement(40, 2)))
        val deleter = RmzDeleter(
            rmzPath = executable(tmp),
            measurer = { measures.removeFirst() },
            processRunner = {
                Result.Success(ProcessResult(1, listOf("Permission denied (os error 13): \"$root/locked\"")))
            },
        )

        val outcome = deleter.delete(root).getOrThrow()

        assertEquals(60, outcome.bytesFreed, "credit before minus surviving remnant")
        assertEquals(3, outcome.entriesDeleted)
        assertEquals(1, outcome.failures)
        assertTrue(outcome.failureSamples.single().contains("os error 13"))
    }

    @Test
    fun `an already-gone root returns EMPTY without invoking rmz`(@TempDir tmp: Path) = runTest {
        var invoked = false
        val deleter = RmzDeleter(
            rmzPath = executable(tmp),
            measurer = { TreeMeasurement(0, 0) }, // 0 entries == root missing
            processRunner = { invoked = true; Result.Success(ProcessResult(0, emptyList())) },
        )

        val outcome = deleter.delete(tmp.resolve("gone")).getOrThrow()

        assertEquals(DeletionOutcome.EMPTY, outcome)
        assertFalse(invoked, "rmz must not run when there is nothing to delete")
    }

    @Test
    fun `fails with RmzUnavailable when the binary is not executable`(@TempDir tmp: Path) = runTest {
        val deleter = RmzDeleter(
            rmzPath = tmp.resolve("no-such-rmz").toString(),
            measurer = { error("must not measure") },
            processRunner = { error("must not exec") },
        )

        val error = (deleter.delete(tmp.resolve("x")) as Result.Failure).throwable

        assertTrue(error is RmzUnavailableException, "got $error")
    }

    @Test
    fun `fails with RmzUnavailable when exec fails`(@TempDir tmp: Path) = runTest {
        val root = Files.createDirectories(tmp.resolve("x"))
        val deleter = RmzDeleter(
            rmzPath = executable(tmp),
            measurer = { TreeMeasurement(1, 1) },
            processRunner = { Result.Failure(IOException("Cannot run program")) },
        )

        val error = (deleter.delete(root) as Result.Failure).throwable

        assertTrue(error is RmzUnavailableException, "got $error")
    }

    @Test
    fun `fails with RmzUnavailable when rmz dies on a signal`(@TempDir tmp: Path) = runTest {
        val root = Files.createDirectories(tmp.resolve("x"))
        val deleter = RmzDeleter(
            rmzPath = executable(tmp),
            measurer = { TreeMeasurement(1, 1) },
            processRunner = { Result.Success(ProcessResult(139, listOf("segfault"))) }, // 128 + SIGSEGV
        )

        val error = (deleter.delete(root) as Result.Failure).throwable

        assertTrue(error is RmzUnavailableException, "got $error")
    }

    @Test
    fun `FileTreeMeasurer reproduces JvmTreeDeleter byte and entry accounting`(@TempDir tmp: Path) = runTest {
        fun tree(name: String): Path {
            val root = Files.createDirectories(tmp.resolve("$name/sub"))
            Files.write(tmp.resolve("$name/a"), ByteArray(111))
            Files.write(tmp.resolve("$name/sub/b"), ByteArray(222))
            Files.createSymbolicLink(tmp.resolve("$name/sub/link"), tmp.resolve("$name/a"))
            return root.parent
        }

        val measured = TreeMeasurerImpl.measure(tree("measured"))
        val deleted = JvmTreeDeleter().delete(tree("deleted")).getOrThrow()

        assertEquals(deleted.bytesFreed, measured.bytes)
        assertEquals(deleted.entriesDeleted, measured.entries)
    }

    private fun executable(tmp: Path): String {
        val f = tmp.resolve("rmz-${System.nanoTime()}")
        Files.write(f, byteArrayOf())
        f.toFile().setExecutable(true)
        return f.toString()
    }
}
