package com.avito.android.async_tc_cleaner.internal.observability

internal interface ReapObserver {
    fun onReaped(entry: String, bytesFreed: Long, entriesDeleted: Long, durationMs: Long)
    fun onError(entryName: String, error: Throwable)
    fun onSweep(summary: ReapSweep) = Unit
    fun onSweepFailed(error: Throwable) = Unit
}

internal object NoOpReapObserver : ReapObserver {
    override fun onReaped(entry: String, bytesFreed: Long, entriesDeleted: Long, durationMs: Long) = Unit
    override fun onError(entryName: String, error: Throwable) = Unit
}

internal data class ReapSweep(
    val durationMs: Long,
    val oldDirPresent: Boolean,
    val claimed: Int,
    val claimMissing: Int,
    val claimFailed: Int,
    val unreadable: Int,
    val deleteAttempted: Int,
    val reaped: Int,
    val incomplete: Int,
    val failed: Int,
    val bytesFreed: Long,
    val entriesDeleted: Long,
    val unlinkFailures: Long,
)
