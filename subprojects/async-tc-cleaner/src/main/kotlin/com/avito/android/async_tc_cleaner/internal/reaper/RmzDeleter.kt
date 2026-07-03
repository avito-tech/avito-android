package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

internal class RmzUnavailableException(message: String) : Exception(message)

internal class RmzDeleter(
    private val rmzPath: String,
    private val measurer: TreeMeasurer = TreeMeasurerImpl,
    private val processRunner: RmzProcessRunner = RmzProcessRunnerImpl,
) : TreeDeleter {

    override suspend fun delete(root: Path): Result<DeletionOutcome> {
        if (!File(rmzPath).canExecute()) {
            return rmzUnavailable("not executable at $rmzPath")
        }

        val before = measurer.measure(root)
        if (before.entries == 0L) {
            return Result.Success(DeletionOutcome.EMPTY)
        }

        val result = processRunner.run(listOf(rmzPath, "-f", "--", root.toString()))
            .getOrElse { e -> return rmzUnavailable("exec failed: ${e.message}") }
        if (result.exitCode >= SIGNAL_EXIT_BASE) {
            return rmzUnavailable("rmz exited on signal ${result.exitCode - SIGNAL_EXIT_BASE}")
        }

        val rootGone = !Files.exists(root, LinkOption.NOFOLLOW_LINKS)
        if (result.exitCode == 0 || rootGone) {
            return Result.Success(
                DeletionOutcome(bytesFreed = before.bytes, entriesDeleted = before.entries, failures = 0)
            )
        }

        val remaining = measurer.measure(root)
        val realErrors = result.stderr.filterNot(::isBenignEnoent)
        val samples = if (realErrors.isEmpty()) {
            listOf("$root (rmz exit ${result.exitCode})")
        } else {
            realErrors.map { "$root ($it)" }
        }
        return Result.Success(
            DeletionOutcome(
                bytesFreed = (before.bytes - remaining.bytes).coerceAtLeast(0),
                entriesDeleted = (before.entries - remaining.entries).coerceAtLeast(0),
                failures = 1,
                failureSamples = samples.limitFailureSamples(),
            )
        )
    }

    private fun rmzUnavailable(message: String): Result<DeletionOutcome> =
        Result.Failure(RmzUnavailableException(message))

    private companion object {
        private const val SIGNAL_EXIT_BASE = 128
    }
}

private fun isBenignEnoent(stderrLine: String): Boolean =
    stderrLine.contains("os error 2") || stderrLine.contains("No such file or directory")
