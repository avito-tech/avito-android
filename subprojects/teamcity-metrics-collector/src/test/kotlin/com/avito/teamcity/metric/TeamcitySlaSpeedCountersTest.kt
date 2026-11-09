package com.avito.teamcity.metric

import com.google.common.truth.Truth.assertThat
import org.jetbrains.teamcity.rest.BuildCanceledInfo
import org.jetbrains.teamcity.rest.BuildStatus
import org.jetbrains.teamcity.rest.User
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Date

class TeamcitySlaSpeedCountersTest {

    private val threshold = 90L

    private fun prBuild(
        duration: Duration,
        status: BuildStatus? = BuildStatus.SUCCESS,
        isFailedToStart: Boolean = false,
        canceledInfo: BuildCanceledInfo? = null,
        targetBranch: String? = "develop",
    ) = StubBuild(
        resultingParameters = listOfNotNull(
            targetBranch?.let { StubParameter(TeamcitySlaSpeedCounters.TARGET_BRANCH_PARAMETER, it) },
            StubParameter("metadata.metrics.tag.unrelated", "value"),
        ),
        status = status,
        isFailedToStart = isFailedToStart,
        canceledInfo = canceledInfo,
        duration = duration,
    )

    @Test
    fun `duration exactly at threshold - count - is total but not bad`() {
        val counters = TeamcitySlaSpeedCounters(threshold)

        counters.count(prBuild(Duration.ofMinutes(threshold)))

        assertThat(counters.total).isEqualTo(1)
        assertThat(counters.bad).isEqualTo(0)
    }

    @Test
    fun `duration one second over threshold - count - is total and bad`() {
        val counters = TeamcitySlaSpeedCounters(threshold)

        counters.count(prBuild(Duration.ofMinutes(threshold).plusSeconds(1)))

        assertThat(counters.total).isEqualTo(1)
        assertThat(counters.bad).isEqualTo(1)
    }

    @Test
    fun `failed build over threshold - count - is total and bad, success and failure are both total`() {
        val counters = TeamcitySlaSpeedCounters(threshold)

        counters.count(prBuild(Duration.ofMinutes(120), status = BuildStatus.FAILURE))
        counters.count(prBuild(Duration.ofMinutes(30), status = BuildStatus.FAILURE))
        counters.count(prBuild(Duration.ofMinutes(30), status = BuildStatus.SUCCESS))

        assertThat(counters.total).isEqualTo(3)
        assertThat(counters.bad).isEqualTo(1)
    }

    @Test
    fun `failed to start build - count - excluded`() {
        val counters = TeamcitySlaSpeedCounters(threshold)

        counters.count(prBuild(Duration.ofMinutes(120), status = BuildStatus.FAILURE, isFailedToStart = true))

        assertThat(counters.total).isEqualTo(0)
        assertThat(counters.bad).isEqualTo(0)
    }

    @Test
    fun `canceled build - count - excluded`() {
        val counters = TeamcitySlaSpeedCounters(threshold)
        @Suppress("OVERRIDE_DEPRECATION")
        val canceled = object : BuildCanceledInfo {
            override val cancelDateTime: ZonedDateTime get() = ZonedDateTime.now()
            override val text: String get() = "canceled"
            override val user: User? get() = null
            override val cancelDate: Date get() = Date()
        }

        counters.count(prBuild(Duration.ofMinutes(120), status = BuildStatus.UNKNOWN, canceledInfo = canceled))
        counters.count(prBuild(Duration.ofMinutes(120), status = BuildStatus.FAILURE, canceledInfo = canceled))

        assertThat(counters.total).isEqualTo(0)
        assertThat(counters.bad).isEqualTo(0)
    }

    @Test
    fun `status other than SUCCESS or FAILURE - count - excluded`() {
        val counters = TeamcitySlaSpeedCounters(threshold)

        counters.count(prBuild(Duration.ofMinutes(120), status = BuildStatus.ERROR))
        counters.count(prBuild(Duration.ofMinutes(120), status = BuildStatus.UNKNOWN))
        counters.count(prBuild(Duration.ofMinutes(120), status = null))

        assertThat(counters.total).isEqualTo(0)
        assertThat(counters.bad).isEqualTo(0)
    }

    @Test
    fun `build without TARGET_BRANCH parameter - count - excluded`() {
        val counters = TeamcitySlaSpeedCounters(threshold)

        counters.count(prBuild(Duration.ofMinutes(120), targetBranch = null))
        counters.count(prBuild(Duration.ofMinutes(120), targetBranch = ""))
        counters.count(prBuild(Duration.ofMinutes(120), targetBranch = "   "))

        assertThat(counters.total).isEqualTo(0)
        assertThat(counters.bad).isEqualTo(0)
    }

    @Test
    fun `no builds - asGraphite - sends explicit zeros with the given timestamp`() {
        val counters = TeamcitySlaSpeedCounters(threshold)
        val now = Instant.parse("2026-10-27T12:00:00Z")

        val metrics = counters.asGraphite(now)

        assertThat(metrics.map { it.path.toString() }).containsExactly("total", "bad").inOrder()
        assertThat(metrics.map { it.value }).containsExactly("0", "0").inOrder()
        assertThat(metrics.map { it.timeInSec }).containsExactly(now.epochSecond, now.epochSecond)
    }

    @Test
    fun `mixed builds - asGraphite - values match counters and timestamp is now, not build time`() {
        val counters = TeamcitySlaSpeedCounters(threshold)
        val now = Instant.parse("2026-10-27T12:00:00Z")
        counters.count(prBuild(Duration.ofMinutes(45)))
        counters.count(prBuild(Duration.ofMinutes(95)))
        counters.count(prBuild(Duration.ofMinutes(100), status = BuildStatus.FAILURE))
        counters.count(prBuild(Duration.ofMinutes(100), targetBranch = null))

        val metrics = counters.asGraphite(now)

        assertThat(metrics.single { it.path.toString() == "total" }.value).isEqualTo("3")
        assertThat(metrics.single { it.path.toString() == "bad" }.value).isEqualTo("2")
        assertThat(metrics.map { it.timeInSec }.distinct()).containsExactly(now.epochSecond)
    }
}
