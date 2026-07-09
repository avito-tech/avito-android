package com.avito.teamcity

import com.avito.android.clickstream.ClickStreamEventTracker
import com.avito.android.graphite.GraphiteSender
import com.avito.teamcity.builds.PreviousMetricsSendingTimeProvider
import com.avito.teamcity.builds.TeamcityBuildsProvider
import com.avito.teamcity.metric.TeamcityBuildDurationMetric
import com.avito.teamcity.metric.TeamcityBuildOverallDurationMetric
import com.avito.teamcity.metric.TeamcityBuildQueueMetric
import com.avito.teamcity.model.TeamcityMetricsSource
import com.avito.teamcity.saturate.bitbucket.BitbucketInfoSaturator
import com.avito.teamcity.saturate.saturateWith
import org.jetbrains.teamcity.rest.Build
import java.time.Instant

internal class SendTeamcityBuildMetricsAction(
    private val teamcityBuildsProvider: TeamcityBuildsProvider,
    private val previousMetricsSendingTimeProvider: PreviousMetricsSendingTimeProvider,
    private val graphiteSenderFactory: (prefix: String) -> GraphiteSender,
    private val clickstreamTracker: ClickStreamEventTracker,
    private val bitbucketInfoSaturator: BitbucketInfoSaturator,
) {

    private val sendersByPrefix = mutableMapOf<String, GraphiteSender>()

    private fun senderFor(prefix: String): GraphiteSender {
        return sendersByPrefix.getOrPut(prefix) { graphiteSenderFactory(prefix) }
    }

    fun execute(metricsSources: List<TeamcityMetricsSource>) {
        val since = previousMetricsSendingTimeProvider.getPreviousSendingTime()
        val until = Instant.now()
        metricsSources.forEach { metricsSource ->
            val graphiteSender = senderFor(metricsSource.metricsPrefix)
            var buildCount = 0
            teamcityBuildsProvider.provide(
                metricsSource = metricsSource,
                since = since,
                until = until,
            ).forEach { build: Build ->
                buildCount++
                graphiteSender.send(
                    TeamcityBuildQueueMetric(build).asGraphite()
                )
                graphiteSender.send(
                    TeamcityBuildDurationMetric(build).asGraphite()
                )
                clickstreamTracker.trackEvent(
                    TeamcityBuildOverallDurationMetric(
                        build.saturateWith(bitbucketInfoSaturator)
                    ).asClickstreamEvent()
                )
                log(build)
            }
            println("Found $buildCount builds for ${metricsSource.configurationId}")
        }
        previousMetricsSendingTimeProvider.saveSendingTime(until)
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
