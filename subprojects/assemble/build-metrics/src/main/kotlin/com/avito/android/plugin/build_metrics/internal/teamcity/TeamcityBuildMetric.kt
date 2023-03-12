package com.avito.android.plugin.build_metrics.internal.teamcity

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.graphite.series.SeriesName
import org.jetbrains.teamcity.rest.Build
import java.time.Duration

internal class TeamcityBuildMetric(
    private val build: Build,
) : BuildMetric.Graphite() {

    private val base: SeriesName = SeriesName.create("teamcity.build", multipart = true)

    override fun asGraphite(): GraphiteMetric {
        val duration = Duration.between(build.startDateTime, build.finishDateTime)
        val status = build.status?.name ?: "unknown"
        val seriesName = base
            .addTag(key = "build_type", value = build.buildConfigurationId.stringId)
            .addTag(key = "status", value = status.lowercase())

        return GraphiteMetric(
            seriesName,
            duration.seconds.toString(),
            build.startDateTime!!.toInstant(),
        )
    }
}
