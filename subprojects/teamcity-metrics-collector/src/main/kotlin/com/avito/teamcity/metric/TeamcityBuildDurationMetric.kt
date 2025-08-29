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
            .addTags(tags = additionalTags(build))

        return GraphiteMetric(
            seriesName,
            duration.seconds.toString(),
            build.startDateTime!!.toInstant(),
        )
    }

    private fun additionalTags(build: Build): Map<String, String> {
        return build.parameters
            .filter { parameter -> parameter.name.startsWith(METRIC_TAGS_PREFIX) }
            .associate { parameter -> parameter.name.substringAfter(METRIC_TAGS_PREFIX) to parameter.value }
    }

    companion object {
        private const val METRIC_TAGS_PREFIX = "metadata.metrics.tag."
    }
}
