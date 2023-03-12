package com.avito.android.plugin.build_metrics.teamcity

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.StubBuildMetricsSender
import com.avito.android.plugin.build_metrics.internal.teamcity.CollectTeamcityMetricsAction
import com.avito.graphite.series.SeriesName
import com.avito.teamcity.TeamcityApi
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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

internal class CollectTeamcityMetricsActionTest {

    private val buildId = "BUILD_ID"

    @Test
    fun `send build metric`() {
        val teamcity: TeamcityApi = mock()

        val build: Build = mock()
        whenever(build.id).thenReturn(BuildId(buildId))
        val startDate = ZonedDateTime.of(
            LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0),
            ZoneId.of("UTC")
        )
        val endDate = startDate.plusSeconds(90)
        whenever(build.buildConfigurationId).thenReturn(BuildConfigurationId("BUILD_TYPE"))
        whenever(build.state).thenReturn(BuildState.FINISHED)
        whenever(build.status).thenReturn(BuildStatus.SUCCESS)
        whenever(build.startDateTime).thenReturn(startDate)
        whenever(build.finishDateTime).thenReturn(endDate)

        whenever(teamcity.getBuild(buildId)).thenReturn(build)

        val sender = StubBuildMetricsSender()
        val action = CollectTeamcityMetricsAction(buildId, teamcity, sender)
        action.execute()

        assertWithMessage("Send a metric about one build")
            .that(sender.getSentGraphiteMetrics()).hasSize(1)

        val metric = sender.getSentGraphiteMetrics().first()
        assertThat(metric).isEqualTo(
            GraphiteMetric(
                path = SeriesName.create("teamcity.build", multipart = true)
                    .addTag("build_type", "BUILD_TYPE")
                    .addTag("status", "success"),
                value = "90",
                time = startDate.toInstant()
            )
        )
    }
}
