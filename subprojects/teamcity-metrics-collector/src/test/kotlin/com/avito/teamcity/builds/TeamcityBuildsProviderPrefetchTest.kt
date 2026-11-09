package com.avito.teamcity.builds

import com.avito.teamcity.metric.TeamcityBuildDurationMetric
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.concurrent.TimeUnit

class TeamcityBuildsProviderPrefetchTest {

    private val since = Instant.parse("2026-02-10T12:00:00Z")
    private val until = Instant.parse("2026-02-10T13:00:00Z")

    @Test
    fun `provide - resulting parameters are prefetched - only one builds request is made`() {
        val build = buildJson(
            id = "1",
            startDate = "20260210T120100+0000",
            finishDate = "20260210T120200+0000",
            resultingProperties = """{ "name": "metadata.metrics.tag.prefetched_tag", "value": "prefetched_value" }""",
        )
        withTeamcityServer(dispatch = { buildsResponse(build) }) { server, provider ->
            val builds = provider.provide(testSource(), since, until).toList()

            val metric = TeamcityBuildDurationMetric(builds.single()).asGraphite()
            assertThat(metric.path.toString()).contains("prefetched_tag=prefetched_value")

            val request = requireNotNull(server.takeRequest(1, TimeUnit.SECONDS)) {
                "Expected TeamCity builds request"
            }
            assertThat(request.path).startsWith("/httpAuth/app/rest/builds?")
            assertThat(request.requestUrl?.queryParameter("fields")).contains("resultingProperties")
            assertThat(server.requestCount).isEqualTo(1)
        }
    }

    @Test
    fun `provide - window includes finish at since, excludes finish at until and after`() {
        val finishedAtSince = buildJson(id = "10", startDate = START_DATE, finishDate = "20260210T120000+0000")
        val finishedAtUntil = buildJson(id = "11", startDate = START_DATE, finishDate = "20260210T130000+0000")
        val finishedAfterUntil = buildJson(id = "12", startDate = START_DATE, finishDate = "20260210T130001+0000")
        withTeamcityServer(
            dispatch = { buildsResponse(finishedAtSince, finishedAtUntil, finishedAfterUntil) }
        ) { _, provider ->
            val builds = provider.provide(testSource(), since, until).toList()

            assertThat(builds.map { it.id.stringId }).containsExactly("10")
        }
    }

    private companion object {
        const val START_DATE = "20260210T110000+0000"
    }
}
