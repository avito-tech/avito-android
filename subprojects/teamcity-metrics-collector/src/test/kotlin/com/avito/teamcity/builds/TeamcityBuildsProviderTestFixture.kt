package com.avito.teamcity.builds

import com.avito.logger.LoggerFactory
import com.avito.logger.NoOpLogger
import com.avito.teamcity.TeamcityApi
import com.avito.teamcity.TeamcityCredentials
import com.avito.teamcity.model.TeamcityMetricsSource
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

internal const val TEST_BUILD_TYPE = "TestBuildType"

internal fun testSource(fetchIntervalInHours: Long = 0) = TeamcityMetricsSource(
    configurationId = TEST_BUILD_TYPE,
    fetchIntervalInHours = fetchIntervalInHours,
    metricsPrefix = "some.namespace",
)

internal fun buildJson(
    id: String,
    startDate: String,
    finishDate: String,
    resultingProperties: String = "",
): String = """
    {
      "id": "$id",
      "buildTypeId": "$TEST_BUILD_TYPE",
      "number": "$id",
      "status": "SUCCESS",
      "state": "finished",
      "startDate": "$startDate",
      "finishDate": "$finishDate",
      "resultingProperties": { "property": [$resultingProperties] }
    }
"""

internal fun buildsResponse(vararg builds: String): MockResponse = MockResponse()
    .setHeader("Content-Type", "application/json")
    .setBody("""{ "build": [${builds.joinToString(",")}] }""")

/**
 * Runs [block] against a [TeamcityBuildsProvider] backed by a MockWebServer;
 * [dispatch] answers every TeamCity request.
 */
internal fun withTeamcityServer(
    dispatch: (RecordedRequest) -> MockResponse,
    block: (server: MockWebServer, provider: TeamcityBuildsProvider) -> Unit,
) {
    val server = MockWebServer()
    server.dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse = dispatch(request)
    }
    server.start()
    try {
        val api = TeamcityApi.create(
            TeamcityCredentials(url = server.url("/").toString().trimEnd('/'), user = "user", password = "password")
        )
        val provider = TeamcityBuildsProvider.create(api, object : LoggerFactory {
            override fun create(tag: String) = NoOpLogger
        })
        block(server, provider)
    } finally {
        server.shutdown()
    }
}
