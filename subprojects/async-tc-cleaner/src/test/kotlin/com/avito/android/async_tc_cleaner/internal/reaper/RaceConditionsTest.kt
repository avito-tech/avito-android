package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.async_tc_cleaner.internal.config.Config
import com.avito.android.async_tc_cleaner.internal.observability.ReapObserver
import com.avito.logger.PrintlnLoggerFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RaceConditionsTest {

    private val logger = PrintlnLoggerFactory.create("race")
    private val deleter = SinglePassTreeDeleter()
    private val excluded = setOf(".old", ".reaper-staging")

    private suspend fun delete(root: Path): DeletionOutcome = root.parent.directoryStream()
        .getOrElse { e -> error(e) }
        .use { stream ->
            deleter.delete(StagedDir(stream, root.fileName, root))
        }

    @Test
    fun `R1 - concurrent deletion of the same tree never throws and converges`(@TempDir tmp: Path) {
        val root = makeWideTree(tmp.resolve("tree"), dirs = 40, filesPerDir = 15)
        val start = CountDownLatch(1)
        val competitor = Thread {
            runCatching {
                start.await()
                Files.walk(root).use { s -> s.forEach { p -> runCatching { Files.deleteIfExists(p) } } }
            }
        }
        competitor.start()
        start.countDown()
        assertDoesNotThrow { runBlocking { delete(root) } }
        competitor.join()
        runBlocking { delete(root) }
        assertFalse(Files.exists(root), "tree must be gone after both deleters + a follow-up sweep")
    }

    @Test
    fun `R2 - a claimed entry is delivered to deletion fully intact`(@TempDir work: Path) = runTest {
        val entry = deadEntry(work, "dead", files = 8)
        val expectedBytes = totalSize(entry)
        val seen = AtomicLong(-1)
        val verifyingDelete = object : TreeDeleter {
            override suspend fun delete(stagedDir: StagedDir): DeletionOutcome {
                seen.set(totalSize(stagedDir.reportedPath))
                return deleter.delete(stagedDir)
            }
        }
        reaper(config(work), Recorder(), verifyingDelete).runOnce()
        assertEquals(expectedBytes, seen.get())
    }

    @Test
    fun `R3 - the old directory itself is never removed`(@TempDir work: Path) = runTest {
        deadEntry(work, "dead")
        reaper(config(work), Recorder()).runOnce()
        assertTrue(Files.isDirectory(work.resolve(".old")), ".old must remain after its contents are reaped")
        assertEquals(0, childCount(work.resolve(".old")))

        deadEntry(work, "dead2")
        reaper(config(work), Recorder()).runOnce()
        assertTrue(Files.isDirectory(work.resolve(".old")))
        assertEquals(0, childCount(work.resolve(".old")))
    }

    @Test
    fun `R4 - reaping only frees the dead tree and changes nothing else`(@TempDir work: Path) = runTest {
        val live = Files.createDirectories(work.resolve("checkout"))
        Files.write(live.resolve("Main.kt"), ByteArray(100))
        val cache = Files.createDirectories(work.resolve("system/.artifacts_cache"))
        Files.write(cache.resolve("dep.jar"), ByteArray(200))
        val freed = totalSize(deadEntry(work, "dead"))

        val before = totalSize(work)
        reaper(config(work), Recorder()).runOnce()
        val after = totalSize(work)

        assertEquals(before - freed, after, "exactly the dead tree's bytes were freed, nothing else")
        assertTrue(Files.exists(live.resolve("Main.kt")) && Files.exists(cache.resolve("dep.jar")))
    }

    @Test
    fun `R5 - symlinks are unlinked, never traversed out of the tree`(@TempDir tmp: Path) = runTest {
        val sentinelDir = Files.createDirectories(tmp.resolve("sentinel"))
        val sentinelFile = Files.write(sentinelDir.resolve("keep"), ByteArray(10))
        val extFile = Files.write(tmp.resolve("ext"), ByteArray(5))
        Files.createDirectories(tmp.resolve("tree/sub"))
        Files.createSymbolicLink(tmp.resolve("tree/sub/dirlink"), sentinelDir)
        Files.createSymbolicLink(tmp.resolve("tree/sub/filelink"), extFile)

        delete(tmp.resolve("tree"))

        assertFalse(Files.exists(tmp.resolve("tree")))
        assertTrue(Files.exists(sentinelDir) && Files.exists(sentinelFile), "symlinked dir target survives")
        assertTrue(Files.exists(extFile), "symlinked file target survives")
    }

    @Test
    fun `R6 - reported bytesFreed equals the tree's real size`(@TempDir tmp: Path) = runTest {
        val root = Files.createDirectories(tmp.resolve("t/x/y"))
        Files.write(tmp.resolve("t/x/a"), ByteArray(111))
        Files.write(tmp.resolve("t/x/y/b"), ByteArray(222))
        Files.write(root.resolve("c"), ByteArray(333))
        val expected = totalSize(tmp.resolve("t"))

        val outcome = delete(tmp.resolve("t"))

        assertEquals(expected, outcome.bytesFreed)
        assertFalse(Files.exists(tmp.resolve("t")))
    }

    @Test
    fun `R7 - orphaned and partially-deleted staging entries are resumed`(@TempDir work: Path) = runTest {
        Files.createDirectories(work.resolve(".reaper-staging/orphan.abc/partial"))
        Files.write(work.resolve(".reaper-staging/orphan.abc/leftover"), ByteArray(10))
        Files.createDirectories(work.resolve(".reaper-staging/whole.def/a/b"))

        val recorder = Recorder()
        reaper(config(work), recorder).runOnce()

        assertEquals(0, childCount(work.resolve(".reaper-staging")), "staging fully drained on resume")
        assertEquals(2, recorder.reaped.size)
    }

    @Test
    fun `R8 - production claim prevents double-claim under a race`(@TempDir work: Path) {
        Files.createDirectories(work.resolve(".old/dead/inner"))
        val start = CountDownLatch(1)
        val claimedRoots = ConcurrentHashMap.newKeySet<String>()
        val deleting = object : TreeDeleter {
            override suspend fun delete(stagedDir: StagedDir): DeletionOutcome {
                claimedRoots.add(stagedDir.stagedEntryRelativePath.toString())
                return DeletionOutcome.EMPTY
            }
        }
        val racers = (0 until 2).map {
            Thread {
                start.await()
                runBlocking {
                    reaper(config(work), Recorder(), deleting).runOnce()
                }
            }
        }
        racers.forEach { it.start() }
        start.countDown()
        racers.forEach { it.join() }

        assertEquals(1, claimedRoots.size, "exactly one reaper claims and deletes the entry")
        assertEquals(
            1,
            childCount(work.resolve(".reaper-staging")),
            "the unremoved staged claim remains visible to retry",
        )
        assertFalse(Files.exists(work.resolve(".old/dead")))
    }

    @Test
    fun `R9 - deletion tolerates a subtree vanishing mid-walk`(@TempDir tmp: Path) {
        Files.createDirectories(tmp.resolve("tree/keep"))
        Files.write(tmp.resolve("tree/keep/f"), ByteArray(10))
        Files.createDirectories(tmp.resolve("tree/vanish/inner"))
        Files.write(tmp.resolve("tree/vanish/inner/g"), ByteArray(10))
        Files.move(tmp.resolve("tree/vanish"), tmp.resolve("moved-away"))

        assertDoesNotThrow { runBlocking { delete(tmp.resolve("tree")) } }

        assertFalse(Files.exists(tmp.resolve("tree")), "root removed despite the vanished subtree")
        assertTrue(Files.exists(tmp.resolve("moved-away/inner/g")), "the moved-away subtree is untouched")
    }

    @Test
    fun `R12 - only the old dir is touched, live checkouts and caches survive`(@TempDir work: Path) = runTest {
        Files.createDirectories(work.resolve("3f8a-checkout/src/main"))
        Files.write(work.resolve("3f8a-checkout/src/main/App.kt"), ByteArray(50))
        Files.createDirectories(work.resolve("system/.artifacts_cache"))
        Files.write(work.resolve("system/.artifacts_cache/a.jar"), ByteArray(60))
        deadEntry(work, "dead")

        val before = snapshotOutsideOld(work)
        reaper(config(work), Recorder()).runOnce()
        val after = snapshotOutsideOld(work)

        assertEquals(before, after, "everything outside .old is byte-for-byte unchanged")
    }

    private class Recorder : ReapObserver {
        val reaped = CopyOnWriteArrayList<String>()
        override fun onReaped(entry: String, bytesFreed: Long, entriesDeleted: Long, durationMs: Long) {
            reaped += entry
        }

        override fun onError(entryName: String, error: Throwable) = Unit
    }

    private fun config(workDir: Path) = Config(
        reaperEnabledToggleFile = null,
        workDir = workDir,
        oldDirName = ".old",
        stagingDirName = ".reaper-staging",
        pollInterval = 50.milliseconds,
        shutdownTimeout = 25.seconds,
        node = "n",
        pod = "p",
        statsd = null,
        elastic = null,
    )

    private fun reaper(
        cfg: Config,
        observer: ReapObserver,
        treeDeleter: TreeDeleter = deleter,
    ) = Reaper(
        config = cfg,
        observer = observer,
        logger = logger,
        deleter = treeDeleter,
    )

    private fun deadEntry(work: Path, name: String, files: Int = 5): Path {
        val leaf = Files.createDirectories(work.resolve(".old/$name/a/b"))
        repeat(files) { Files.write(work.resolve(".old/$name/a/f$it"), ByteArray(10)) }
        Files.write(leaf.resolve("deep"), ByteArray(20))
        return work.resolve(".old/$name")
    }

    private fun makeWideTree(root: Path, dirs: Int, filesPerDir: Int): Path {
        Files.createDirectories(root)
        repeat(dirs) { d ->
            val dir = Files.createDirectories(root.resolve("d$d"))
            repeat(filesPerDir) { f -> Files.write(dir.resolve("f$f"), ByteArray(8)) }
        }
        return root
    }

    private fun totalSize(root: Path): Long {
        var sum = 0L
        Files.walk(root).use { s ->
            s.forEach { if (Files.isRegularFile(it, LinkOption.NOFOLLOW_LINKS)) sum += Files.size(it) }
        }
        return sum
    }

    private fun childCount(dir: Path): Int = Files.newDirectoryStream(dir).use { stream ->
        return stream.count()
    }

    private fun snapshotOutsideOld(work: Path): Map<String, Long> {
        val map = HashMap<String, Long>()
        Files.walk(work).use { s ->
            s.forEach { p ->
                if (Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
                    val rel = work.relativize(p).toString()
                    if (excluded.none { rel.startsWith(it) }) {
                        map[rel] = Files.size(p)
                    }
                }
            }
        }
        return map
    }
}
