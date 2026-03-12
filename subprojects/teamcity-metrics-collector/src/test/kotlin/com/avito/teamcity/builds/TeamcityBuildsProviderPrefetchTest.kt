package com.avito.teamcity.builds

import com.avito.logger.LoggerFactory
import com.avito.logger.NoOpLogger
import com.avito.teamcity.TeamcityApi
import com.avito.teamcity.TeamcityCredentials
import com.avito.teamcity.metric.TeamcityBuildDurationMetric
import com.avito.teamcity.model.TeamcityMetricsSource
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.concurrent.TimeUnit

class TeamcityBuildsProviderPrefetchTest {

    @Test
    fun `provide - resulting parameters are prefetched - only one builds request is made`() {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(BUILDS_LIST_JSON)
        )
        server.start()

        try {
            val api = TeamcityApi.create(
                TeamcityCredentials(
                    url = server.url("/").toString().trimEnd('/'),
                    user = "user",
                    password = "password",
                )
            )

            val provider = TeamcityBuildsProvider.create(api, object : LoggerFactory {
                override fun create(tag: String) = NoOpLogger
            })
            val builds = provider.provide(
                metricsSource = TeamcityMetricsSource(
                    configurationId = "TestBuildType",
                    fetchIntervalInHours = 0,
                ),
                since = Instant.parse("2026-02-10T12:00:00Z"),
                until = Instant.parse("2026-02-10T13:00:00Z"),
            ).toList()

            val metric = TeamcityBuildDurationMetric(builds.single()).asGraphite()
            assertThat(metric.path.toString()).contains("prefetched_tag=prefetched_value")

            val request = requireNotNull(server.takeRequest(1, TimeUnit.SECONDS)) {
                "Expected TeamCity builds request"
            }
            assertThat(request.path).startsWith("/httpAuth/app/rest/builds?")
            assertThat(request.requestUrl?.queryParameter("fields")).contains("resultingProperties")

            assertThat(server.requestCount).isEqualTo(1)
            assertThat(server.takeRequest(200, TimeUnit.MILLISECONDS)).isNull()
        } finally {
            server.shutdown()
        }
    }

    private companion object {
        // Only fields used by TeamcityBuildsProvider + TeamcityBuildDurationMetric are included.
        private const val BUILD_JSON: String = """
            {
              "id": "1",
              "buildTypeId": "TestBuildType",
              "number": "1",
              "status": "SUCCESS",
              "state": "finished",
              "startDate": "20260210T120100+0000",
              "finishDate": "20260210T120200+0000",
              "resultingProperties": {
                "property": [
                  { "name": "metadata.metrics.tag.prefetched_tag", "value": "prefetched_value" }
                ]
              }
            }
        """

        private const val BUILDS_LIST_JSON: String = """
            {
              "build": [
                $BUILD_JSON
              ]
            }
        """
    }
}
