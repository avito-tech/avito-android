package com.avito.android.plugin.build_metrics

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.graphite.GraphiteSender
import com.avito.android.stats.graphiteSeries
import com.avito.android.stats.graphiteSeriesElement
import com.avito.teamcity.TeamcityApi
import org.jetbrains.teamcity.rest.Build
import java.time.Duration

@Suppress("UnstableApiUsage")
class CollectTeamcityMetricsAction(
    val buildId: String,
    private val teamcityApi: TeamcityApi,
    private val graphite: GraphiteSender
) {

    fun execute() {
        val build = teamcityApi.getBuild(buildId)
        sendMetric(build)
    }

    private fun sendMetric(build: Build) {
        val buildId = graphiteSeriesElement(build.id.stringId)
        val buildTypeId = graphiteSeries(build.buildConfigurationId.stringId)
        val buildStatus = graphiteSeriesElement(build.status?.name ?: "unknown")
        val duration = Duration.between(build.startDateTime, build.finishDateTime)

        // We use a redundant structure only for compatibility reasons
        val path = "ci.builds.teamcity.duration" +
            ".build_type_id.$buildTypeId" +
            ".id.$buildId" +
            ".agent._" +
            ".state._" +
            ".status.$buildStatus" +
            "._._._._"
        graphite.send(GraphiteMetric(path, duration.seconds.toString(), build.startDateTime!!.toEpochSecond()))
    }
}
