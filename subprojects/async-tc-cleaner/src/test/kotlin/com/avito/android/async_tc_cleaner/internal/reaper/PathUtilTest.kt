package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class PathUtilTest {

    @Test
    fun `opens an existing directory and lists its entries`(@TempDir tmp: Path) {
        val dir = Files.createDirectories(tmp.resolve("dir"))
        Files.write(dir.resolve("a"), ByteArray(1))
        Files.write(dir.resolve("b"), ByteArray(1))

        val names = dir.directoryStream().getOrThrow().use { stream ->
            stream.map { it.fileName.toString() }.toSet()
        }

        assertEquals(setOf("a", "b"), names)
    }

    @Test
    fun `does not follow a directory replaced by a symlink`(@TempDir tmp: Path) {
        val sentinel = Files.createDirectories(tmp.resolve("sentinel"))
        val victim = Files.write(sentinel.resolve("victim"), ByteArray(10))
        val link = Files.createSymbolicLink(tmp.resolve("dir"), sentinel)

        val result = link.directoryStream()

        assertTrue(result is Result.Failure, "a symlinked directory must not be opened")
        assertTrue(Files.exists(victim), "the symlink target's contents must survive")
    }

    @Test
    fun `fails on a missing directory without throwing`(@TempDir tmp: Path) {
        val result = tmp.resolve("does-not-exist").directoryStream()

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `fails on a path that is a regular file`(@TempDir tmp: Path) {
        val file = Files.write(tmp.resolve("file"), ByteArray(1))

        val result = file.directoryStream()

        assertTrue(result is Result.Failure)
    }
}
