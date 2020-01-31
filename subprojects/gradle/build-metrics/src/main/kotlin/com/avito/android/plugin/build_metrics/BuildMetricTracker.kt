package com.avito.android.plugin.build_metrics

import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StatsMetric
import com.avito.android.stats.graphiteSeries
import org.gradle.BuildResult
import org.gradle.api.provider.Provider

@Suppress("UnstableApiUsage")
class BuildMetricTracker(
    private val env: Provider<EnvironmentInfo>,
    private val sender: Provider<StatsDSender>
) {

    fun track(buildResult: BuildResult, metric: StatsMetric) {
        val prefix = graphiteSeries(
            env.get().environment.publicName,
            node,
            "id",
            buildStatus(buildResult)
        )
        sender.get().send(prefix, metric)
    }

    fun track(metric: StatsMetric) {
        val prefix = graphiteSeries(
            env.get().environment.publicName,
            node,
            "id"
        )
        sender.get().send(prefix, metric)
    }

    private fun buildStatus(buildResult: BuildResult): String {
        return if (buildResult.failure == null) "success" else "fail"
    }

    private val node by lazy {
        env.get().node?.take(32) ?: "unknown"
    }
}
