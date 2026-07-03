package com.avito.android.async_tc_cleaner.internal.reaper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

internal data class TreeMeasurement(val bytes: Long, val entries: Long)

internal fun interface TreeMeasurer {
    suspend fun measure(root: Path): TreeMeasurement
}

internal object TreeMeasurerImpl : TreeMeasurer {

    override suspend fun measure(root: Path): TreeMeasurement = withContext(Dispatchers.IO) {
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
            return@withContext TreeMeasurement(0, 0)
        }
        var bytes = 0L
        var entries = 0L
        try {
            Files.walk(root).use { stream ->
                val iterator = stream.iterator()
                while (iterator.hasNext()) {
                    currentCoroutineContext().ensureActive()
                    entries++
                    bytes += fileBytes(iterator.next())
                }
            }
        } catch (_: UncheckedIOException) {
        }
        TreeMeasurement(bytes, entries)
    }

    private fun fileBytes(path: Path): Long = try {
        val attrs = Files.readAttributes(path, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        if (attrs.isDirectory || attrs.isSymbolicLink) 0 else attrs.size()
    } catch (_: IOException) {
        0
    }
}
