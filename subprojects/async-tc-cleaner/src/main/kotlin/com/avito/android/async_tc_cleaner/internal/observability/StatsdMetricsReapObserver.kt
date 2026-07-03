package com.avito.android.async_tc_cleaner.internal.observability

import com.avito.android.async_tc_cleaner.internal.config.StatsdSettings
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StatsMetric
import com.avito.graphite.series.SeriesName
import com.avito.logger.LoggerFactory

internal class StatsdMetricsReapObserver internal constructor(
    private val sender: StatsDSender,
    private val node: String,
) : ReapObserver {

    override fun onReaped(entry: String, bytesFreed: Long, entriesDeleted: Long, durationMs: Long) {
        sender.send(StatsMetric.time(metric("reaped_bytes"), bytesFreed / BYTES_PER_MIB))
        sender.send(StatsMetric.time(metric("reaped_duration_ms"), durationMs))
        sender.send(StatsMetric.count(metric("reaped_count")))
        sender.send(StatsMetric.count(metric("reaped_bytes_total"), bytesFreed))
        sender.send(StatsMetric.count(metric("reaped_entries_total"), entriesDeleted))
    }

    override fun onError(entry: String, error: Throwable) {
        sender.send(StatsMetric.count(metric("errors")))
    }

    override fun onSweep(summary: ReapSweep) {
        sender.send(StatsMetric.count(metric("sweep_count")))
        sender.send(StatsMetric.time(metric("sweep_duration_ms"), summary.durationMs))
        sender.send(StatsMetric.count(metric("sweep_claimed"), summary.claimed.toLong()))
        sender.send(StatsMetric.count(metric("sweep_claim_missing"), summary.claimMissing.toLong()))
        sender.send(StatsMetric.count(metric("sweep_claim_failed"), summary.claimFailed.toLong()))
        sender.send(StatsMetric.count(metric("sweep_unreadable"), summary.unreadable.toLong()))
        sender.send(StatsMetric.count(metric("sweep_reaped"), summary.reaped.toLong()))
        sender.send(StatsMetric.count(metric("sweep_incomplete"), summary.incomplete.toLong()))
        sender.send(StatsMetric.count(metric("sweep_failed"), summary.failed.toLong()))
        sender.send(StatsMetric.count(metric("sweep_unlink_failures"), summary.unlinkFailures))
    }

    override fun onSweepFailed(error: Throwable) {
        sender.send(StatsMetric.count(metric("sweep_error")))
    }

    private fun metric(leaf: String): SeriesName = SeriesName.create(node).append(leaf)

    companion object {
        private const val BYTES_PER_MIB = 1024L * 1024L

        fun create(settings: StatsdSettings, node: String, loggerFactory: LoggerFactory): StatsdMetricsReapObserver {
            val config = StatsDConfig.Enabled(
                host = settings.host,
                fallbackHost = settings.fallbackHost,
                port = settings.port,
                namespace = SeriesName.create(settings.namespace, multipart = true),
            )
            return StatsdMetricsReapObserver(StatsDSender.create(config, loggerFactory), node)
        }
    }
}
