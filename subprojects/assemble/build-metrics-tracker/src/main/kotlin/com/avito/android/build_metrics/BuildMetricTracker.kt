package com.avito.android.build_metrics

import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.CountMetric
import com.avito.android.stats.GaugeDoubleMetric
import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StatsMetric
import com.avito.android.stats.TimeMetric
import com.avito.android.stats.statsd
import com.avito.utils.gradle.Environment
import org.gradle.api.Project

// TODO: Use statsd client directly and pass required prefix from client/plugin
public interface BuildMetricTracker {

    public fun track(status: BuildStatus, metric: StatsMetric)
    public fun track(metric: StatsMetric)

    public companion object {

        public fun from(project: Project): BuildMetricTracker {
            val statsd = project.statsd.get()
            val environmentInfo = project.environmentInfo().get()

            return from(statsd, environmentInfo)
        }

        public fun from(statsd: StatsDSender, environmentInfo: EnvironmentInfo): BuildMetricTracker =
            BuildMetricTrackerImpl(environmentInfo, statsd)
    }
}

internal class BuildMetricTrackerImpl(
    private val environmentInfo: EnvironmentInfo,
    private val statsd: StatsDSender
) : BuildMetricTracker {

    override fun track(status: BuildStatus, metric: StatsMetric) {
        val prefix = SeriesName.create(
            environment(),
            node(),
            "id", // for backward compatibility
            seriesName(status)
        )
        statsd.send(metric.withPrefix(prefix))
    }

    override fun track(metric: StatsMetric) {
        val prefix = SeriesName.create(
            environment(),
            node(),
        )
        statsd.send(metric.withPrefix(prefix))
    }

    private fun environment(): String {
        return when (environmentInfo.environment) {
            Environment.Local -> "local"
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
