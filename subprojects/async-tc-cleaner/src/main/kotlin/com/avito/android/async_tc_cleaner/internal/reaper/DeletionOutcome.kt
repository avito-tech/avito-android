package com.avito.android.async_tc_cleaner.internal.reaper

import java.nio.file.Path

internal data class DeletionOutcome(
    val bytesFreed: Long,
    val entriesDeleted: Long,
    val failures: Long = 0,
    val failureSamples: List<String> = emptyList(),
) {
    operator fun plus(other: DeletionOutcome) =
        DeletionOutcome(
            bytesFreed + other.bytesFreed,
            entriesDeleted + other.entriesDeleted,
            failures + other.failures,
            (failureSamples + other.failureSamples).limitFailureSamples(),
        )

    companion object {
        val EMPTY = DeletionOutcome(0, 0)

        fun failure(path: Path, error: Throwable): DeletionOutcome = DeletionOutcome(
            bytesFreed = 0,
            entriesDeleted = 0,
            failures = 1,
            failureSamples = listOf(failureSample(path, error)),
        )
    }
}
