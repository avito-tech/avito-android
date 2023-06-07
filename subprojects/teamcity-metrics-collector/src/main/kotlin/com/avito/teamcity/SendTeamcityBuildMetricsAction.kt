package com.avito.teamcity

import com.avito.android.graphite.GraphiteSender
import com.avito.teamcity.builds.TeamcityBuildsProvider
import com.avito.teamcity.metric.TeamcityBuildDurationMetric
import com.avito.teamcity.metric.TeamcityBuildQueueMetric
import org.jetbrains.teamcity.rest.Build

internal class SendTeamcityBuildMetricsAction(
    private val teamcityBuildsProvider: TeamcityBuildsProvider,
    private val graphiteSender: GraphiteSender,
) {

    fun execute(metricsSourceBuildType: String) {
        teamcityBuildsProvider.provide(metricsSourceBuildType).forEach { build: Build ->
            graphiteSender.send(
                TeamcityBuildQueueMetric(build).asGraphite()
            )
            graphiteSender.send(
                TeamcityBuildDurationMetric(build).asGraphite()
            )
            log(build)
        }
    }

    private fun log(build: Build) {
        println(
            buildString {
                appendLine("Sent build metrics for ${build.buildNumber}")
                appendLine("Status ${build.status}")
                appendLine("queued at ${build.queuedDateTime}")
                appendLine("start at ${build.startDateTime}")
                appendLine("finished at ${build.finishDateTime}")
            }
        )
    }
}
