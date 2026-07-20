package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import com.avito.android.async_tc_cleaner.internal.observability.ReapObserver
import com.avito.logger.Logger
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.IOException
import java.nio.file.DirectoryIteratorException
import java.nio.file.LinkOption
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.SecureDirectoryStream
import java.nio.file.attribute.BasicFileAttributeView
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.measureTimedValue

internal class StagedDeleter(
    private val stagingDir: Path,
    private val deleter: TreeDeleter,
    private val observer: ReapObserver,
    private val logger: Logger,
) {

    suspend fun deleteAll(stagingDirStream: SecureDirectoryStream<Path>): DeleteStats {
        var stats = DeleteStats()
        try {
            for (stagedDir in stagingDirStream) {
                currentCoroutineContext().ensureActive()
                stats += deleteStagedEntry(
                    stagingDirStream = stagingDirStream,
                    stagedEntryRelativePath = stagedDir.fileName
                )
            }
        } catch (e: DirectoryIteratorException) {
            logger.warn("Failed to iterate staging dir: dir=$stagingDir", e.cause ?: e)
            stats += DeleteStats(
                failed = 1,
                outcome = DeletionOutcome(
                    0,
                    0,
                    failureSamples = listOf(
                        FailureSamplesUtil.format(
                            path = stagingDir,
                            error = e.cause ?: e
                        )
                    )
                ),
            )
        }
        return stats
    }

    private suspend fun deleteStagedEntry(
        stagingDirStream: SecureDirectoryStream<Path>,
        stagedEntryRelativePath: Path,
    ): DeleteStats {
        val stagedDirAbsolutePath = stagingDir.resolve(stagedEntryRelativePath)
        val name = stagedEntryRelativePath.toString().substringBeforeLast('.')
        val context = "entry=$name, staged=$stagedDirAbsolutePath"

        logger.info("Reap started: $context")
        val (result, duration) = measureTimedValue {
            return@measureTimedValue try {
                Result.Success(
                    deleter.delete(
                        StagedDir(
                            stagingDirStream = stagingDirStream,
                            stagedEntryRelativePath = stagedEntryRelativePath,
                            reportedPath = stagedDirAbsolutePath
                        )
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Failure<DeletionOutcome>(e)
            }
        }

        val durationMs = duration.inWholeMilliseconds
        return result.fold(
            onSuccess = { outcome ->
                val remnant = existsInStaging(stagingDirStream, stagedEntryRelativePath)
                if (outcome.failures > 0L || remnant) {
                    onDeleteIncomplete(name, context, stagedDirAbsolutePath, durationMs, outcome, remnant)
                } else {
                    onDeleteSucceeded(name, context, durationMs, outcome)
                }
            },
            onFailure = { error -> onDeleteFailed(name, context, stagedDirAbsolutePath, durationMs, error) },
        )
    }

    private fun existsInStaging(
        stagingStream: SecureDirectoryStream<Path>,
        stagedEntryRelativePath: Path,
    ): Boolean = try {
        stagingStream.getFileAttributeView(
            stagedEntryRelativePath,
            BasicFileAttributeView::class.java,
            LinkOption.NOFOLLOW_LINKS,
        ).readAttributes()
        true
    } catch (_: NoSuchFileException) {
        false
    } catch (_: IOException) {
        true
    }

    private fun onDeleteFailed(
        name: String,
        context: String,
        staged: Path,
        durationMs: Long,
        error: Throwable,
    ): DeleteStats {
        logger.warn("Reap failed: $context, durationMs=$durationMs", error)
        observer.onError(name, error)
        return DeleteStats(
            attempted = 1,
            failed = 1,
            outcome = DeletionOutcome(
                bytesFreed = 0,
                entriesDeleted = 0,
                failureSamples = listOf(
                    FailureSamplesUtil.format(staged, error)
                )
            ),
        )
    }

    private fun onDeleteIncomplete(
        name: String,
        context: String,
        staged: Path,
        durationMs: Long,
        outcome: DeletionOutcome,
        remnant: Boolean,
    ): DeleteStats {
        val samples = if (remnant) {
            FailureSamplesUtil.capped(outcome.failureSamples + "$staged (staged root still present)")
        } else {
            outcome.failureSamples
        }
        logger.warn(
            "Reap incomplete: $context, durationMs=$durationMs, bytesFreed=${outcome.bytesFreed}, " +
                "entriesDeleted=${outcome.entriesDeleted}, failures=${outcome.failures}, remnant=$remnant, " +
                "failureSamples=${samples.joinToString("; ")}"
        )
        observer.onError(
            entryName = name,
            error = IOException(reapIncompleteReason(staged, outcome.failures, remnant, samples)),
        )
        return DeleteStats(
            attempted = 1,
            incomplete = 1,
            outcome = outcome.copy(failureSamples = samples),
        )
    }

    private fun onDeleteSucceeded(
        name: String,
        context: String,
        durationMs: Long,
        outcome: DeletionOutcome,
    ): DeleteStats {
        logger.info(
            "Reap succeeded: $context, durationMs=$durationMs, bytesFreed=${outcome.bytesFreed}, " +
                "entriesDeleted=${outcome.entriesDeleted}"
        )
        observer.onReaped(name, outcome.bytesFreed, outcome.entriesDeleted, durationMs)
        return DeleteStats(
            attempted = 1,
            reaped = 1,
            outcome = outcome,
        )
    }

    private fun reapIncompleteReason(
        staged: Path,
        failures: Long,
        remnant: Boolean,
        samples: List<String>,
    ): String = buildString {
        append("Reap incomplete for $staged: $failures unlink failure(s)")
        if (remnant) append(", staged root still present")
        if (samples.isNotEmpty()) append(", samples=${samples.joinToString("; ")}")
    }
}
