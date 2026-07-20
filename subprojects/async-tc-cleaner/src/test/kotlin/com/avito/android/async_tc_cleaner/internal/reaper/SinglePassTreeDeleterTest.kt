package com.avito.android.async_tc_cleaner.internal.reaper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class SinglePassTreeDeleterTest {

    private val deleter = SinglePassTreeDeleter()

    private suspend fun delete(root: Path): DeletionOutcome = root.parent.directoryStream()
        .getOrElse { e -> error(e) }
        .use { stream ->
            deleter.delete(StagedDir(stream, root.fileName, root))
        }

    @Test
    fun `counts every node once and only regular-file bytes`(@TempDir tmp: Path) = runTest {
        val root = Files.createDirectories(tmp.resolve("tree"))
        Files.write(root.resolve("a"), ByteArray(100))
        Files.createSymbolicLink(root.resolve("link"), root.resolve("a"))
        val sub = Files.createDirectories(root.resolve("sub"))
        Files.write(sub.resolve("b"), ByteArray(50))

        val outcome = delete(root)

        assertEquals(150L, outcome.bytesFreed, "only the two regular files' bytes; symlink and dirs are 0")
        assertEquals(5L, outcome.entriesDeleted, "a, link, b, sub, root")
        assertEquals(0L, outcome.failures)
        assertFalse(Files.exists(root))
    }

    @Test
    fun `a cancelled deletion stops mid-walk and leaves a remnant`(@TempDir tmp: Path) {
        val dirs = 200
        val root = makeWideTree(tmp.resolve("tree"), dirs = dirs, filesPerDir = 25)
        runBlocking {
            val job = launch(Dispatchers.IO) { delete(root) }
            while (job.isActive && childCount(root) == dirs) delay(1)
            job.cancelAndJoin()
            assertTrue(job.isCancelled, "cancellation propagates out of the deleter")
        }
        assertTrue(Files.exists(root), "a cancelled walk leaves a remnant for the next sweep")
    }

    @Test
    fun `a vanished subtree is not counted and is not a failure`(@TempDir tmp: Path) = runTest {
        Files.createDirectories(tmp.resolve("tree/keep"))
        Files.write(tmp.resolve("tree/keep/f"), ByteArray(100))
        Files.createDirectories(tmp.resolve("tree/vanish/inner"))
        Files.write(tmp.resolve("tree/vanish/inner/g"), ByteArray(50))
        Files.move(tmp.resolve("tree/vanish"), tmp.resolve("moved-away"))

        val outcome = delete(tmp.resolve("tree"))

        assertEquals(0L, outcome.failures, "a vanished entry is a normal race, not a failure")
        assertEquals(100L, outcome.bytesFreed, "only what actually existed is credited")
        assertEquals(3L, outcome.entriesDeleted, "keep/f, keep, tree — not the moved-away subtree")
        assertTrue(Files.exists(tmp.resolve("moved-away/inner/g")), "the moved-away subtree is untouched")
    }

    @Test
    fun `a dangling symlink is unlinked`(@TempDir tmp: Path) = runTest {
        val root = Files.createDirectories(tmp.resolve("tree"))
        Files.write(root.resolve("real"), ByteArray(10))
        Files.createSymbolicLink(root.resolve("dangle"), root.resolve("does-not-exist"))

        val outcome = delete(root)

        assertFalse(Files.exists(root), "the dangling link and the tree are gone")
        assertEquals(0L, outcome.failures)
        assertEquals(3L, outcome.entriesDeleted, "real, dangle, root")
        assertEquals(10L, outcome.bytesFreed, "the dangling link contributes 0 bytes")
    }

    @Test
    fun `a symlink to an external file is unlinked without touching the target`(@TempDir tmp: Path) = runTest {
        val external = Files.write(tmp.resolve("external"), ByteArray(7))
        val root = Files.createDirectories(tmp.resolve("tree"))
        Files.createSymbolicLink(root.resolve("ext"), external)

        delete(root)

        assertFalse(Files.exists(root))
        assertTrue(Files.exists(external), "the external target survives")
        assertEquals(7L, Files.size(external))
    }

    @Test
    fun `a fifo nested in the tree is unlinked and does not block its parent`(@TempDir tmp: Path) = runTest {
        val root = Files.createDirectories(tmp.resolve("tree"))
        Files.write(root.resolve("f"), ByteArray(10))
        mkfifo(root.resolve("pipe"))

        val outcome = delete(root)

        assertFalse(Files.exists(root), "the fifo did not block deletion of its ancestor")
        assertEquals(0L, outcome.failures)
        assertEquals(3L, outcome.entriesDeleted, "f, pipe, root")
        assertEquals(10L, outcome.bytesFreed, "the fifo contributes 0 bytes")
    }

    @Test
    fun `a fifo as the staged root is unlinked`(@TempDir tmp: Path) = runTest {
        val fifo = tmp.resolve("root-fifo")
        mkfifo(fifo)

        val outcome = delete(fifo)

        assertFalse(Files.exists(fifo))
        assertEquals(1L, outcome.entriesDeleted)
        assertEquals(0L, outcome.failures)
    }

    @Test
    fun `a missing root yields an empty outcome`(@TempDir tmp: Path) = runTest {
        val outcome = delete(tmp.resolve("never-existed"))

        assertEquals(DeletionOutcome.EMPTY, outcome)
    }

    @Test
    fun `a regular file as the staged root is unlinked and counted`(@TempDir tmp: Path) = runTest {
        val file = Files.write(tmp.resolve("root-file"), ByteArray(42))

        val outcome = delete(file)

        assertFalse(Files.exists(file))
        assertEquals(1L, outcome.entriesDeleted)
        assertEquals(42L, outcome.bytesFreed)
    }

    private fun mkfifo(path: Path) {
        val exit = ProcessBuilder("mkfifo", path.toString()).inheritIO().start().waitFor()
        check(exit == 0) { "mkfifo failed with exit $exit for $path" }
    }

    private fun makeWideTree(root: Path, dirs: Int, filesPerDir: Int): Path {
        Files.createDirectories(root)
        repeat(dirs) { d ->
            val dir = Files.createDirectories(root.resolve("d$d"))
            repeat(filesPerDir) { f -> Files.write(dir.resolve("f$f"), ByteArray(8)) }
        }
        return root
    }

    private fun childCount(dir: Path): Int = Files.newDirectoryStream(dir).use { it.count() }
}
