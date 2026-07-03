package com.avito.android.async_tc_cleaner.internal.reaper

import java.io.IOException
import java.nio.file.DirectoryIteratorException
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

internal class DirectoryScanner {

    fun scan(dir: Path): DirectoryEntries {
        var scanFailures = DeletionOutcome.EMPTY
        val leaves = ArrayList<Leaf>()
        val childDirs = ArrayList<Path>()
        try {
            Files.newDirectoryStream(dir).use { stream: DirectoryStream<Path> ->
                for (entry in stream) {
                    when (val read = readAttrs(entry)) {
                        is AttrReadResult.Found -> {
                            val attrs = read.attrs
                            when {
                                attrs.isSymbolicLink -> leaves.add(Leaf(entry, size = 0))
                                attrs.isDirectory -> childDirs.add(entry)
                                else -> leaves.add(Leaf(entry, size = attrs.size()))
                            }
                        }
                        is AttrReadResult.Failed -> scanFailures += DeletionOutcome.failure(entry, read.error)
                        AttrReadResult.Missing -> Unit
                    }
                }
            }
        } catch (_: NoSuchFileException) {
            return DirectoryEntries.EMPTY
        } catch (e: DirectoryIteratorException) {
            scanFailures += DeletionOutcome.failure(dir, e.cause ?: e)
        } catch (e: IOException) {
            scanFailures += DeletionOutcome.failure(dir, e)
        }
        return DirectoryEntries(
            leaves = leaves,
            childDirs = childDirs,
            scanFailures = scanFailures,
        )
    }

    fun readAttrs(path: Path): AttrReadResult = try {
        AttrReadResult.Found(Files.readAttributes(path, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS))
    } catch (_: NoSuchFileException) {
        AttrReadResult.Missing
    } catch (e: IOException) {
        AttrReadResult.Failed(e)
    }
}

internal data class DirectoryEntries(
    val leaves: List<Leaf>,
    val childDirs: List<Path>,
    val scanFailures: DeletionOutcome,
) {
    companion object {
        val EMPTY = DirectoryEntries(
            leaves = emptyList(),
            childDirs = emptyList(),
            scanFailures = DeletionOutcome.EMPTY,
        )
    }
}

internal data class Leaf(val path: Path, val size: Long)

internal sealed class AttrReadResult {
    data class Found(val attrs: BasicFileAttributes) : AttrReadResult()
    data class Failed(val error: IOException) : AttrReadResult()
    object Missing : AttrReadResult()
}
