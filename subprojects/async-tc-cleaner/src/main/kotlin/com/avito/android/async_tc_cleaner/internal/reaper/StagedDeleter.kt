package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import com.avito.android.async_tc_cleaner.internal.observability.ReapObserver
import com.avito.logger.Logger
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.IOException
import java.nio.file.DirectoryIteratorException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.TimeSource

internal class StagedDeleter(
    private val stagingDir: Path,
    private val deleter: TreeDeleter,
    private val observer: ReapObserver,
    private val logger: Logger,
) {

    suspend fun deleteAll(): DeleteStats {
        val stream = stagingDir.directoryStream().getOrElse { e ->
            logger.warn("Failed to list staging dir: dir=$stagingDir", e)
            return DeleteStats(
                failed = 1,
                failureSamples = listOf(failureSample(stagingDir, e)),
            )
        }
        var stats = DeleteStats()

        stream.use {
            try {
                for (staged in it) {
                    currentCoroutineContext().ensureActive()
                    stats += deleteStagedEntry(staged)
                }
            } catch (e: DirectoryIteratorException) {
                logger.warn("Failed to iterate staging dir: dir=$stagingDir", e.cause ?: e)
                stats += DeleteStats(
                    failed = 1,
                    failureSamples = listOf(failureSample(stagingDir, e.cause ?: e)),
                )
            }
        }
        return stats
    }

    private suspend fun deleteStagedEntry(staged: Path): DeleteStats {
        val name = staged.fileName.toString().substringBeforeLast('.')
        val context = reapContext(name, staged)

        val start = TimeSource.Monotonic.markNow()
        logger.info("Reap started: $context")

        val deletionResult = try {
            deleter.delete(staged)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Failure(e)
        }

        val durationMs = start.elapsedNow().inWholeMilliseconds
        return deletionResult.fold(
            onSuccess = { outcome ->
                val remnant = Files.exists(staged, LinkOption.NOFOLLOW_LINKS)
                if (outcome.failures > 0L || remnant) {
                    onDeleteIncomplete(name, context, staged, durationMs, outcome, remnant)
                } else {
                    onDeleteSucceeded(name, context, durationMs, outcome)
                }
            },
            onFailure = { error -> onDeleteFailed(name, context, staged, durationMs, error) },
        )
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
            failureSamples = listOf(failureSample(staged, error)),
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
            (outcome.failureSamples + "$staged (staged root still present)").limitFailureSamples()
        } else {
            outcome.failureSamples
        }
        logger.warn(
            "Reap incomplete: $context, durationMs=$durationMs, bytesFreed=${outcome.bytesFreed}, " +
                "entriesDeleted=${outcome.entriesDeleted}, failures=${outcome.failures}, remnant=$remnant, " +
                "failureSamples=${samples.joinToString("; ")}"
        )
        observer.onError(name, IOException(reapIncompleteReason(staged, outcome.failures, remnant, samples)))
        return DeleteStats(
            attempted = 1,
            incomplete = 1,
            bytesFreed = outcome.bytesFreed,
            entriesDeleted = outcome.entriesDeleted,
            failures = outcome.failures,
            failureSamples = samples,
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
            bytesFreed = outcome.bytesFreed,
            entriesDeleted = outcome.entriesDeleted,
        )
    }

    private fun reapContext(name: String, staged: Path): String = "entry=$name, staged=$staged"

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
