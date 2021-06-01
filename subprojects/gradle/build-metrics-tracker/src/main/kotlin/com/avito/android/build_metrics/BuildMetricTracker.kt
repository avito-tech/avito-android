package com.avito.android.build_metrics

import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.CountMetric
import com.avito.android.stats.GaugeDoubleMetric
import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StatsMetric
import com.avito.android.stats.TimeMetric
import com.avito.utils.gradle.Environment

public class BuildMetricTracker(
    private val environmentInfo: EnvironmentInfo,
    private val sender: StatsDSender
) {

    public fun track(status: BuildStatus, metric: StatsMetric) {
        val prefix = SeriesName.create(
            environment(),
            node(),
            "id", // for backward compatibility
            seriesName(status)
        )
        sender.send(metric.withPrefix(prefix))
    }

    public fun track(metric: StatsMetric) {
        val prefix = SeriesName.create(
            environment(),
            node(),
            // build on the last position to not interfere with environments
            "build"
        )
        sender.send(metric.withPrefix(prefix))
    }

    private fun environment(): String {
        return when (environmentInfo.environment) {
            Environment.Local -> "local"
            Environment.Mirakle -> "mirakle"
            Environment.CI -> "ci"
            Environment.Unknown -> "_"
        }
    }

    private fun node(): String {
        return when (environmentInfo.environment) {
            // Don't need details from CI because build agents are ephemeral
            is Environment.CI -> "_"
            else -> environmentInfo.node ?: "unknown"
        }
    }

    private fun seriesName(status: BuildStatus): String {
        return when (status) {
            is BuildStatus.Success -> "success"
            is BuildStatus.Fail -> "fail"
        }
    }

    private fun StatsMetric.withPrefix(prefix: SeriesName): StatsMetric {
        return when (this) {
            is TimeMetric -> this.copy(name.prefix(prefix))
            is CountMetric -> this.copy(name.prefix(prefix))
            is GaugeDoubleMetric -> this.copy(name.prefix(prefix))
            is GaugeLongMetric -> this.copy(name.prefix(prefix))
        }
    }
}
