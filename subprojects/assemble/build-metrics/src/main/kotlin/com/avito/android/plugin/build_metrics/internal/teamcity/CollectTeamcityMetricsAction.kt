package com.avito.android.plugin.build_metrics.internal.teamcity

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.graphite.GraphiteSender
import com.avito.android.stats.SeriesName
import com.avito.teamcity.TeamcityApi
import org.jetbrains.teamcity.rest.Build
import java.time.Duration

internal class CollectTeamcityMetricsAction(
    val buildId: String,
    private val teamcityApi: TeamcityApi,
    private val graphite: GraphiteSender
) {

    fun execute() {
        val build = teamcityApi.getBuild(buildId)
        sendMetric(build)
    }

    private fun sendMetric(build: Build) {
        val duration = Duration.between(build.startDateTime, build.finishDateTime)

        // We use a redundant structure only for compatibility reasons
        val path = SeriesName.create(
            "ci",
            "builds",
            "teamcity",
            "duration",
            "build_type_id",
            build.buildConfigurationId.stringId,
            "id",
            build.id.stringId,
            "agent",
            "_",
            "state",
            "_",
            "status",
            build.status?.name ?: "unknown",
            "_",
            "_",
            "_",
            "_"
        )

        try {
            graphite.send(
                GraphiteMetric(
                    path.toString(),
                    duration.seconds.toString(),
                    build.startDateTime!!.toEpochSecond()
                )
            )
        } catch (e: Exception) {
            // TODO send alert
        }
    }
}
