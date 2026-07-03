package com.avito.android.async_tc_cleaner.internal.observability

import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StatsMetric
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StatsdMetricsReapObserverTest {

    private class FakeSender : StatsDSender {
        val names = mutableListOf<String>()
        override fun send(metric: StatsMetric) {
            names += metric.name.toString()
        }
    }

    private val sender = FakeSender()
    private val observer = StatsdMetricsReapObserver(sender, "node")

    @Test
    fun `onReaped emits the reaped metric family`() {
        observer.onReaped(entry = "e", bytesFreed = 5L * 1024 * 1024, entriesDeleted = 10, durationMs = 42)

        assertEmitted(
            "reaped_count",
            "reaped_bytes",
            "reaped_duration_ms",
            "reaped_bytes_total",
            "reaped_entries_total",
        )
    }

    @Test
    fun `onError emits the errors counter`() {
        observer.onError("e", RuntimeException("boom"))

        assertEmitted("errors")
    }

    @Test
    fun `onSweep emits the sweep counter family`() {
        observer.onSweep(sweep())

        assertEmitted("sweep_count", "sweep_duration_ms", "sweep_reaped", "sweep_failed", "sweep_unlink_failures")
    }

    @Test
    fun `onSweepFailed emits the sweep_error counter`() {
        observer.onSweepFailed(RuntimeException("boom"))

        assertEmitted("sweep_error")
    }

    private fun assertEmitted(vararg leaves: String) {
        leaves.forEach { leaf ->
            val found = sender.names.any { it.contains(leaf) }
            assertTrue(found, "expected a metric containing '$leaf', got ${sender.names}")
        }
    }

    private fun sweep() = ReapSweep(
        durationMs = 1,
        oldDirPresent = true,
        claimed = 0,
        claimMissing = 0,
        claimFailed = 0,
        unreadable = 0,
        deleteAttempted = 0,
        reaped = 0,
        incomplete = 0,
        failed = 0,
        bytesFreed = 0,
        entriesDeleted = 0,
        unlinkFailures = 0,
    )
}
