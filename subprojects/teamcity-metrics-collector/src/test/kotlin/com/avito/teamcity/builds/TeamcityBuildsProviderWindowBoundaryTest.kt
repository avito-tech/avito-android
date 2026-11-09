package com.avito.teamcity.builds

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * TeamCity REST reports `finishDate` with second precision, while the pass boundaries
 * (`since` = previous cursor, `until`) are instants. Two passes with adjoining windows must count
 * a finished build exactly once, whichever side of the request moment its real finish fell on.
 *
 * The server mock models the verified TeamCity behaviour: `sinceDate`/`untilDate` filter by
 * start date and only finished builds are returned, so a build still running at the request
 * moment is absent from the response.
 */
class TeamcityBuildsProviderWindowBoundaryTest {

    private val pass1Since = Instant.parse("2026-02-10T12:00:00Z")
    private val pass1Until = Instant.parse("2026-02-10T13:00:00Z")
    private val pass2Until = Instant.parse("2026-02-10T14:00:00Z")
    private val pass1RequestMoment = pass1Until.plusMillis(700)

    @Test
    fun `build finishing in the until second after the request - counted exactly once across two passes`() {
        val seen = runTwoPasses(actualFinish = pass1Until.plusMillis(900))

        assertThat(seen).containsExactly("1")
    }

    @Test
    fun `build finishing in the until second before the request - counted exactly once across two passes`() {
        val seen = runTwoPasses(actualFinish = pass1Until.plusMillis(300))

        assertThat(seen).containsExactly("1")
    }

    private fun runTwoPasses(actualFinish: Instant): List<String> {
        var requestMoment = pass1RequestMoment
        val build = buildJson(id = "1", startDate = "20260210T123000+0000", finishDate = "20260210T130000+0000")
        val seen = mutableListOf<String>()
        withTeamcityServer(
            dispatch = { if (actualFinish.isBefore(requestMoment)) buildsResponse(build) else buildsResponse() }
        ) { _, provider ->
            seen += provider.provide(testSource(), pass1Since, pass1Until).map { it.id.stringId }.toList()
            requestMoment = pass2Until.plusMillis(700)
            seen += provider.provide(testSource(), pass1Until, pass2Until).map { it.id.stringId }.toList()
        }
        return seen
    }
}
