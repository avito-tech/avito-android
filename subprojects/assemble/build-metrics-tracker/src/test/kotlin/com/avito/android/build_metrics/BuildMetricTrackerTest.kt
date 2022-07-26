package com.avito.android.build_metrics

import com.avito.android.sentry.StubEnvironmentInfo
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StubStatsdSender
import com.avito.android.stats.TimeMetric
import com.avito.utils.gradle.Environment
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class BuildMetricTrackerTest {

    @Test
    fun `build - build status`() {
        val statsd = statsd()
        val tracker = metricTracker(
            statsd = statsd,
            environment = Environment.Local,
            node = "user"
        )
        tracker.track(BuildStatus.Success, TimeMetric(SeriesName.create("metric"), 1))

        val metrics = statsd.getSentMetrics()
        assertThat(metrics).hasSize(1)
        assertThat(metrics.first().name.toString()).isEqualTo("local.user.id.success.metric")
    }

    private fun statsd() = StubStatsdSender()

    private fun metricTracker(
        statsd: StatsDSender,
        environment: Environment,
        node: String,
    ): BuildMetricTracker {
        val envInfo = StubEnvironmentInfo(
            environment = environment,
            node = node
        )
        return BuildMetricTrackerImpl(envInfo, statsd)
    }
}
