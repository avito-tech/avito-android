package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.logger.Logger
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.DirectoryIteratorException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID

internal class Claimer(
    private val oldDir: Path,
    private val stagingDir: Path,
    private val logger: Logger,
) {

    suspend fun claimEligible(): ClaimStats {
        if (!Files.isDirectory(oldDir, LinkOption.NOFOLLOW_LINKS)) {
            return ClaimStats(oldDirPresent = false)
        }

        val stream = oldDir.directoryStream().getOrElse { e ->
            logger.warn("Failed to list old dir for claim: dir=$oldDir", e)
            return ClaimStats(oldDirPresent = true, unreadable = 1)
        }
        var stats = ClaimStats(oldDirPresent = true)

        stream.use {
            try {
                for (entry in it) {
                    currentCoroutineContext().ensureActive()
                    stats += claim(entry)
                }
            } catch (e: DirectoryIteratorException) {
                logger.warn("Failed to iterate old dir for claim: dir=$oldDir", e.cause ?: e)
                stats += ClaimStats(unreadable = 1)
            }
        }
        return stats
    }

    private fun claim(entry: Path): ClaimStats {
        val target = stagingDir.resolve("${entry.fileName}.${UUID.randomUUID()}")
        try {
            Files.move(entry, target, StandardCopyOption.ATOMIC_MOVE)
            logger.info("Reap claim succeeded: source=$entry, target=$target")
            return ClaimStats(claimed = 1)
        } catch (_: NoSuchFileException) {
            logger.info("Reap claim skipped: source=$entry, reason=missing")
            return ClaimStats(missing = 1)
        } catch (e: AtomicMoveNotSupportedException) {
            logger.warn("Reap claim failed: source=$entry, target=$target, reason=atomic_move_not_supported", e)
            return ClaimStats(failed = 1)
        } catch (e: IOException) {
            logger.warn("Reap claim failed: source=$entry, target=$target", e)
            return ClaimStats(failed = 1)
        }
    }
}
