package com.avito.teamcity.builds

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * `fetchIntervalInHours` must be at least the longest possible build plus an hour.
 *
 * TeamCity `sinceDate` filters by start date (inclusive) and only finished builds are returned,
 * so the provider asks for builds started after `since - fetchIntervalInHours`. A build that
 * started earlier than that is never returned to any hourly pass and silently disappears from
 * both per-build metrics and SLA counters. The fixture is a build slightly longer than three hours
 * finishing right after an hour boundary — the worst position inside the hour.
 */
class LongBuildFetchIntervalTest {

    private val start = Instant.parse("2026-10-30T07:59:00Z")
    private val finish = Instant.parse("2026-10-30T11:01:00Z")
    private val build = buildJson(
        id = "1",
        startDate = "20261030T075900+0000",
        finishDate = "20261030T110100+0000",
        resultingProperties = """{ "name": "env.TARGET_BRANCH", "value": "develop" }""",
    )

    @Test
    fun `build longer than fetchIntervalInHours 3 - lost by every hourly pass`() {
        assertThat(seenByHourlyPasses(fetchIntervalInHours = 3)).isEmpty()
    }

    @Test
    fun `build shorter than fetchIntervalInHours 4 - counted exactly once`() {
        assertThat(seenByHourlyPasses(fetchIntervalInHours = 4)).containsExactly("1")
    }

    @Test
    fun `build shorter than fetchIntervalInHours 5 - counted exactly once`() {
        assertThat(seenByHourlyPasses(fetchIntervalInHours = 5)).containsExactly("1")
    }

    private fun seenByHourlyPasses(fetchIntervalInHours: Long): List<String> {
        var serverNow = start
        val seen = mutableListOf<String>()
        withTeamcityServer(
            dispatch = { request ->
                val locator = requireNotNull(request.requestUrl?.queryParameter("locator"))
                val sinceText = requireNotNull(Regex("sinceDate:([^,]+)").find(locator)).groupValues[1]
                val sinceDate = OffsetDateTime.parse(sinceText, TEAMCITY_DATE_FORMAT).toInstant()
                val returned = !start.isBefore(sinceDate) && !finish.isAfter(serverNow)
                if (returned) buildsResponse(build) else buildsResponse()
            }
        ) { _, provider ->
            for (hour in 9..14) {
                serverNow = Instant.parse("2026-10-30T${hour.toString().padStart(2, '0')}:00:00Z")
                seen += provider.provide(testSource(fetchIntervalInHours), serverNow.minusSeconds(3600), serverNow)
                    .map { it.id.stringId }.toList()
            }
        }
        return seen
    }

    private companion object {
        val TEAMCITY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ")
    }
}
