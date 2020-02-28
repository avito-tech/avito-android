package com.avito.android.plugin.build_metrics

import com.avito.android.stats.FakeStatsdSender
import com.avito.teamcity.TeamcityApi
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.jetbrains.teamcity.rest.Build
import org.jetbrains.teamcity.rest.BuildConfigurationId
import org.jetbrains.teamcity.rest.BuildId
import org.jetbrains.teamcity.rest.BuildState
import org.jetbrains.teamcity.rest.BuildStatus
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime

@Suppress("UnstableApiUsage")
class CollectTeamcityMetricsActionTest {

    @Test
    fun `send build metric`() {
        val teamcity: TeamcityApi = mock()
        val statsd = FakeStatsdSender()

        val build: Build = mock()
        whenever(build.id).thenReturn(BuildId(buildId))
        val startDate = ZonedDateTime.of(
            LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0),
            ZoneId.systemDefault()
        )
        val endDate = startDate.plusSeconds(90)
        whenever(build.buildConfigurationId).thenReturn(BuildConfigurationId("BUILD_TYPE"))
        whenever(build.state).thenReturn(BuildState.FINISHED)
        whenever(build.status).thenReturn(BuildStatus.SUCCESS)
        whenever(build.startDateTime).thenReturn(startDate)
        whenever(build.finishDateTime).thenReturn(endDate)

        whenever(teamcity.getBuild(buildId)).thenReturn(build)

        val action = CollectTeamcityMetricsAction(buildId, teamcity, statsd)
        action.execute()

        assertWithMessage("Send a metric about one build")
            .that(statsd.metrics).hasSize(1)

        assertThat(statsd.paths.first()).isEmpty()
        val metric = statsd.metrics.first()
        assertThat(metric.path).isEqualTo(
            "ci.builds.teamcity.duration.build_type_id.BUILD_TYPE.id.BUILD_ID.agent._.state._.status.SUCCESS._._._._"
        )
        assertThat(metric.value).isEqualTo(90_000)
    }

    private val buildId = "BUILD_ID"

}
