package com.avito.teamcity.metric

import com.avito.android.graphite.GraphiteMetric
import com.avito.graphite.series.SeriesName
import org.jetbrains.teamcity.rest.Build
import java.time.Duration

internal class TeamcityBuildDurationMetric(
    private val build: Build,
) {

    private val base: SeriesName = SeriesName.create("teamcity.build", multipart = true)

    fun asGraphite(): GraphiteMetric {
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
