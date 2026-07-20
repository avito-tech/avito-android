package com.avito.android.async_tc_cleaner.internal.reaper

import java.nio.file.Path

internal data class DeletionOutcome(
    val bytesFreed: Long,
    val entriesDeleted: Long,
    val failures: Long = 0,
    val failureSamples: List<String> = emptyList(),
) {
    operator fun plus(other: DeletionOutcome): DeletionOutcome = DeletionOutcome(
        bytesFreed = bytesFreed + other.bytesFreed,
        entriesDeleted = entriesDeleted + other.entriesDeleted,
        failures = failures + other.failures,
        failureSamples = FailureSamplesUtil.capped(failureSamples + other.failureSamples),
    )

    companion object {
        val EMPTY = DeletionOutcome(0, 0)

        fun deleted(sizeBytes: Long): DeletionOutcome = DeletionOutcome(
            bytesFreed = sizeBytes,
            entriesDeleted = 1,
        )

        fun failure(reportedPath: Path, error: Throwable): DeletionOutcome = DeletionOutcome(
            bytesFreed = 0,
            entriesDeleted = 0,
            failures = 1,
            failureSamples = listOf(FailureSamplesUtil.format(reportedPath, error)),
        )
    }
}
