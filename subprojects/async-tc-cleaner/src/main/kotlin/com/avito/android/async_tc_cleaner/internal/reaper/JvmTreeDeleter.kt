package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal class JvmTreeDeleter(
    private val scanner: DirectoryScanner = DirectoryScanner(),
) : TreeDeleter {

    override suspend fun delete(root: Path): Result<DeletionOutcome> = withContext(Dispatchers.IO) {
        Result.Success(
            when (val read = scanner.readAttrs(root)) {
                is AttrReadResult.Found -> {
                    val attrs = read.attrs
                    if (attrs.isSymbolicLink || !attrs.isDirectory) {
                        deleteLeaf(root, sizeOf = if (attrs.isSymbolicLink) 0 else attrs.size())
                    } else {
                        deleteDirectory(root)
                    }
                }

                is AttrReadResult.Failed -> DeletionOutcome.failure(root, read.error)
                AttrReadResult.Missing -> DeletionOutcome.EMPTY
            }
        )
    }

    private suspend fun deleteDirectory(dir: Path): DeletionOutcome {
        currentCoroutineContext().ensureActive()
        val entries = scanner.scan(dir)
        var outcome = entries.scanFailures
        for (leaf in entries.leaves) {
            currentCoroutineContext().ensureActive()
            outcome += deleteLeaf(leaf.path, leaf.size)
        }
        for (child in entries.childDirs) {
            outcome += deleteDirectory(child)
        }
        currentCoroutineContext().ensureActive()
        return outcome + deleteLeaf(dir, sizeOf = 0)
    }

    private fun deleteLeaf(path: Path, sizeOf: Long): DeletionOutcome = try {
        if (Files.deleteIfExists(path)) {
            DeletionOutcome(bytesFreed = sizeOf, entriesDeleted = 1)
        } else {
            DeletionOutcome.EMPTY
        }
    } catch (e: IOException) {
        DeletionOutcome.failure(path, e)
    }
}
