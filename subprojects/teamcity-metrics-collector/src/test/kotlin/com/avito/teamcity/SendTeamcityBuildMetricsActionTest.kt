package com.avito.teamcity

import com.avito.android.Result
import com.avito.android.clickstream.ClickStreamEventTracker
import com.avito.android.clickstream.ClickStreamSender
import com.avito.android.clickstream.api.ClickStreamEventRequest
import com.avito.android.graphite.GraphiteMetric
import com.avito.android.graphite.GraphiteSender
import com.avito.teamcity.builds.PreviousMetricsSendingTimeProvider
import com.avito.teamcity.builds.TeamcityBuildsProvider
import com.avito.teamcity.metric.StubBuild
import com.avito.teamcity.metric.StubParameter
import com.avito.teamcity.metric.TeamcitySlaSpeedCounters
import com.avito.teamcity.model.TeamcityMetricsSource
import com.avito.teamcity.saturate.bitbucket.BitbucketInfoSaturator
import com.google.common.truth.Truth.assertThat
import org.jetbrains.teamcity.rest.Build
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class SendTeamcityBuildMetricsActionTest {

    private val events = mutableListOf<String>()

    private val source = TeamcityMetricsSource(
        configurationId = "AvitoAndroid_Build",
        fetchIntervalInHours = 3,
        metricsPrefix = "apps.mobile.statistic.android",
        sla = TeamcityMetricsSource.SlaConfig(
            thresholdMinutes = 90,
            metricsPrefix = "apps.mobile.statistic.sla.android.speed",
        ),
    )

    private val prBuild = StubBuild(
        resultingParameters = listOf(StubParameter(TeamcitySlaSpeedCounters.TARGET_BRANCH_PARAMETER, "develop")),
        duration = Duration.ofMinutes(100),
    )

    private val clock = Clock.fixed(Instant.parse("2026-10-29T13:34:56.789Z"), ZoneOffset.UTC)

    private fun action(slaSenderFailsOn: String? = null) = SendTeamcityBuildMetricsAction(
        teamcityBuildsProvider = object : TeamcityBuildsProvider {
            override fun provide(
                metricsSource: TeamcityMetricsSource,
                since: Instant,
                until: Instant,
            ): Sequence<Build> {
                events += "provide until=$until"
                return sequenceOf(prBuild)
            }
        },
        previousMetricsSendingTimeProvider = object : PreviousMetricsSendingTimeProvider {
            override fun getPreviousSendingTime(): Instant = Instant.parse("2026-10-29T12:00:00Z")
            override fun saveSendingTime(time: Instant) {
                events += "save"
                events += "cursor=$time"
            }
        },
        graphiteSenderFactory = { prefix ->
            object : GraphiteSender {
                override fun send(metric: GraphiteMetric) {
                    val path = metric.path.toString()
                    if (prefix == source.sla?.metricsPrefix && path == slaSenderFailsOn) {
                        throw IllegalStateException("Fail to create Socket")
                    }
                    events += "send $prefix $path"
                }
            }
        },
        clickstreamTracker = ClickStreamEventTracker(
            clickStreamSender = object : ClickStreamSender {
                override fun sendEvents(envelope: ClickStreamEventRequest): Result<Unit> = Result.Success(Unit)
            }
        ),
        bitbucketInfoSaturator = BitbucketInfoSaturator(bitbucketHost = "http://localhost", bitbucketToken = "token"),
        clock = clock,
    )

    @Test
    fun `execute - until and the saved cursor are whole seconds`() {
        action().execute(listOf(source))

        assertThat(events).contains("provide until=2026-10-29T13:34:56Z")
        assertThat(events).contains("cursor=2026-10-29T13:34:56Z")
    }

    @Test
    fun `execute - sla counters are sent after the cursor is saved, per-build metrics before it`() {
        action().execute(listOf(source))

        val saveIndex = events.indexOf("save")
        val perBuildSends = events.filter { it.startsWith("send ${source.metricsPrefix} ") }
        val slaSends = events.filter { it.startsWith("send ${source.sla?.metricsPrefix} ") }

        assertThat(perBuildSends).hasSize(2)
        assertThat(perBuildSends.all { events.indexOf(it) < saveIndex }).isTrue()
        assertThat(slaSends).containsExactly(
            "send ${source.sla?.metricsPrefix} total",
            "send ${source.sla?.metricsPrefix} bad",
        ).inOrder()
        assertThat(slaSends.all { events.indexOf(it) > saveIndex }).isTrue()
    }

    @Test
    fun `execute - sla send fails - cursor is already saved, so a retry does not count the same builds twice`() {
        assertThrows<IllegalStateException> {
            action(slaSenderFailsOn = "total").execute(listOf(source))
        }

        assertThat(events).contains("save")
        assertThat(events.none { it.startsWith("send ${source.sla?.metricsPrefix} ") }).isTrue()
    }
}
