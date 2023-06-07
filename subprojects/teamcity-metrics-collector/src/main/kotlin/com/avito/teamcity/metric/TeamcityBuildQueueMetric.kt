package com.avito.teamcity.metric

import com.avito.android.graphite.GraphiteMetric
import com.avito.graphite.series.SeriesName
import org.jetbrains.teamcity.rest.Build
import java.time.Duration

internal class TeamcityBuildQueueMetric(
    private val build: Build,
) {

    private val base: SeriesName = SeriesName.create("teamcity.queue", multipart = true)

    fun asGraphite(): GraphiteMetric {
        val duration = Duration.between(build.queuedDateTime, build.startDateTime)
        val seriesName = base
            .addTag(key = "build_type", value = build.buildConfigurationId.stringId)

        return GraphiteMetric(
            seriesName,
            duration.seconds.toString(),
            build.queuedDateTime.toInstant(),
        )
    }
}
