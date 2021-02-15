package com.avito.android.plugin.build_metrics

import com.avito.android.sentry.EnvironmentInfo
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StatsMetric
import org.gradle.api.provider.Provider

public class BuildMetricTracker(
    private val env: Provider<EnvironmentInfo>,
    private val sender: Provider<StatsDSender>
) {

    private val node by lazy {
        env.get().node?.take(32) ?: "unknown"
    }

    public fun track(buildStatus: BuildStatus, metric: StatsMetric) {
        val prefix = SeriesName.create(
            env.get().environment.publicName,
            node,
            "id",
            buildStatus.name
        )
        sender.get().send(prefix, metric)
    }

    public fun track(metric: StatsMetric) {
        val prefix = SeriesName.create(
            env.get().environment.publicName,
            node,
            "id"
        )
        sender.get().send(prefix, metric)
    }

    public sealed class BuildStatus(public val name: String) {
        public object Success : BuildStatus("success")
        public object Fail : BuildStatus("fail")
    }
}
