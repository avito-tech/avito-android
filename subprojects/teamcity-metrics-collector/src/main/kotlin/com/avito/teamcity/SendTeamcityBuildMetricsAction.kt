package com.avito.teamcity

import com.avito.android.clickstream.ClickStreamEventTracker
import com.avito.android.graphite.GraphiteSender
import com.avito.teamcity.builds.PreviousMetricsSendingTimeProvider
import com.avito.teamcity.builds.TeamcityBuildsProvider
import com.avito.teamcity.metric.TeamcityBuildDurationMetric
import com.avito.teamcity.metric.TeamcityBuildOverallDurationMetric
import com.avito.teamcity.metric.TeamcityBuildQueueMetric
import com.avito.teamcity.metric.TeamcitySlaSpeedCounters
import com.avito.teamcity.model.TeamcityMetricsSource
import com.avito.teamcity.saturate.bitbucket.BitbucketInfoSaturator
import com.avito.teamcity.saturate.saturateWith
import org.jetbrains.teamcity.rest.Build
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

internal class SendTeamcityBuildMetricsAction(
    private val teamcityBuildsProvider: TeamcityBuildsProvider,
    private val previousMetricsSendingTimeProvider: PreviousMetricsSendingTimeProvider,
    private val graphiteSenderFactory: (prefix: String) -> GraphiteSender,
    private val clickstreamTracker: ClickStreamEventTracker,
    private val bitbucketInfoSaturator: BitbucketInfoSaturator,
    private val clock: Clock = Clock.systemUTC(),
) {

    private val sendersByPrefix = mutableMapOf<String, GraphiteSender>()

    private fun senderFor(prefix: String): GraphiteSender {
        return sendersByPrefix.getOrPut(prefix) { graphiteSenderFactory(prefix) }
    }

    fun execute(metricsSources: List<TeamcityMetricsSource>) {
        val since = previousMetricsSendingTimeProvider.getPreviousSendingTime()
        val until = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS)
        val pendingSlaCounters = mutableListOf<Pair<TeamcityMetricsSource, TeamcitySlaSpeedCounters>>()
        metricsSources.forEach { metricsSource ->
            val graphiteSender = senderFor(metricsSource.metricsPrefix)
            val slaCounters = metricsSource.sla?.let { TeamcitySlaSpeedCounters(it.thresholdMinutes) }
            var buildCount = 0
            teamcityBuildsProvider.provide(
                metricsSource = metricsSource,
                since = since,
                until = until,
            ).forEach { build: Build ->
                buildCount++
                slaCounters?.count(build)
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
            if (slaCounters != null) {
                pendingSlaCounters += metricsSource to slaCounters
            }
        }
        previousMetricsSendingTimeProvider.saveSendingTime(until)
        pendingSlaCounters.forEach { (metricsSource, counters) -> sendSlaCounters(metricsSource, counters) }
    }

    /**
     * SLA counters are sent with `timestamp = now` on purpose: the tech platform registry collector
     * reads Graphite hour by hour at HH:02 and never re-reads a collected hour, so a point stamped with
     * the build start time would land in an hour that is already closed.
     * Zeros are sent too — an idle pass must be "0 of 0", not a gap.
     */
    private fun sendSlaCounters(metricsSource: TeamcityMetricsSource, counters: TeamcitySlaSpeedCounters) {
        val sla = requireNotNull(metricsSource.sla)
        val sender = senderFor(sla.metricsPrefix)
        counters.asGraphite(now = Instant.now(clock)).forEach(sender::send)
        println(
            "Sent SLA speed counters for ${metricsSource.configurationId} to ${sla.metricsPrefix}: " +
                "total=${counters.total}, bad=${counters.bad} (threshold ${sla.thresholdMinutes} min)"
        )
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
