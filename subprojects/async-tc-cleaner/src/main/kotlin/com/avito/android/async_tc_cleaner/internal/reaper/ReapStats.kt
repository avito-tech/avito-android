package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.async_tc_cleaner.internal.observability.ReapSweep
import java.nio.file.Path
import kotlin.time.Duration

private const val MAX_FAILURE_SAMPLES: Int = 5

internal fun failureSample(path: Path, error: Throwable): String {
    val reason = listOfNotNull(
        error.javaClass.simpleName,
        error.message?.takeIf { it.isNotBlank() },
    ).joinToString(": ")
    return "$path ($reason)"
}

internal fun List<String>.limitFailureSamples(): List<String> = take(MAX_FAILURE_SAMPLES)

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
        bytesFreed = deletes.bytesFreed,
        entriesDeleted = deletes.entriesDeleted,
        unlinkFailures = deletes.failures,
    )
}

internal data class DeleteStats(
    val attempted: Int = 0,
    val reaped: Int = 0,
    val incomplete: Int = 0,
    val failed: Int = 0,
    val bytesFreed: Long = 0,
    val entriesDeleted: Long = 0,
    val failures: Long = 0,
    val failureSamples: List<String> = emptyList(),
) {
    operator fun plus(other: DeleteStats): DeleteStats = copy(
        attempted = attempted + other.attempted,
        reaped = reaped + other.reaped,
        incomplete = incomplete + other.incomplete,
        failed = failed + other.failed,
        bytesFreed = bytesFreed + other.bytesFreed,
        entriesDeleted = entriesDeleted + other.entriesDeleted,
        failures = failures + other.failures,
        failureSamples = (failureSamples + other.failureSamples).limitFailureSamples(),
    )
}
