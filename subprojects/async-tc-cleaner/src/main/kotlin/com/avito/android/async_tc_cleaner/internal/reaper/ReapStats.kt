package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.async_tc_cleaner.internal.observability.ReapSweep
import kotlin.time.Duration

internal data class ClaimStats(
    val oldDirPresent: Boolean = false,
    val claimed: Int = 0,
    val missing: Int = 0,
    val failed: Int = 0,
    val unreadable: Int = 0,
) {
    operator fun plus(other: ClaimStats): ClaimStats = ClaimStats(
        oldDirPresent = oldDirPresent || other.oldDirPresent,
        claimed = claimed + other.claimed,
        missing = missing + other.missing,
        failed = failed + other.failed,
        unreadable = unreadable + other.unreadable,
    )

    fun toSweep(deletes: DeleteStats, duration: Duration): ReapSweep = ReapSweep(
        durationMs = duration.inWholeMilliseconds,
        oldDirPresent = oldDirPresent,
        claimed = claimed,
        claimMissing = missing,
        claimFailed = failed,
        unreadable = unreadable,
        deleteAttempted = deletes.attempted,
        reaped = deletes.reaped,
        incomplete = deletes.incomplete,
        failed = deletes.failed,
        bytesFreed = deletes.outcome.bytesFreed,
        entriesDeleted = deletes.outcome.entriesDeleted,
        unlinkFailures = deletes.outcome.failures,
    )
}

internal data class DeleteStats(
    val attempted: Int = 0,
    val reaped: Int = 0,
    val incomplete: Int = 0,
    val failed: Int = 0,
    val outcome: DeletionOutcome = DeletionOutcome.EMPTY,
) {
    val bytesFreed: Long get() = outcome.bytesFreed
    val entriesDeleted: Long get() = outcome.entriesDeleted
    val failures: Long get() = outcome.failures
    val failureSamples: List<String> get() = outcome.failureSamples

    operator fun plus(other: DeleteStats): DeleteStats = DeleteStats(
        attempted = attempted + other.attempted,
        reaped = reaped + other.reaped,
        incomplete = incomplete + other.incomplete,
        failed = failed + other.failed,
        outcome = outcome + other.outcome,
    )
}
