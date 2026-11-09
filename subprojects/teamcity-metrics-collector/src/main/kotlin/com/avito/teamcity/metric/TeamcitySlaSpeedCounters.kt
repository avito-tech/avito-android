package com.avito.teamcity.metric

import com.avito.android.graphite.GraphiteMetric
import com.avito.graphite.series.SeriesName
import org.jetbrains.teamcity.rest.Build
import org.jetbrains.teamcity.rest.BuildStatus
import java.time.Duration
import java.time.Instant

/**
 * Per-pass SLA counters for the "PR build speed" scenario of the tech platform registry (Plato).
 *
 * `total` — finished PR builds with status SUCCESS or FAILURE;
 * `bad` — those of them whose duration (start → finish) is longer than [thresholdMinutes].
 *
 * Excluded from both counters: canceled builds, builds that failed to start and
 * builds without a non-blank `env.TARGET_BRANCH` resulting parameter (not a PR build).
 *
 * Both counters are sent with the *current* timestamp, not the build timestamp:
 * the registry collector reads Graphite only for the previous hour and treats late points as missing.
 * Zero values are sent too, so that an idle hour is "0 of 0" and not a gap.
 */
internal class TeamcitySlaSpeedCounters(
    thresholdMinutes: Long,
) {
    private val threshold: Duration = Duration.ofMinutes(thresholdMinutes)
    private val totalSeries: SeriesName = SeriesName.create(TOTAL, multipart = false)
    private val badSeries: SeriesName = SeriesName.create(BAD, multipart = false)

    var total: Int = 0
        private set

    var bad: Int = 0
        private set

    fun count(build: Build) {
        if (!build.isEligible()) return
        total++
        if (build.duration() > threshold) {
            bad++
        }
    }

    fun asGraphite(now: Instant): List<GraphiteMetric> {
        return listOf(
            GraphiteMetric(totalSeries, total.toString(), now),
            GraphiteMetric(badSeries, bad.toString(), now),
        )
    }

    private fun Build.isEligible(): Boolean {
        if (isFailedToStart || canceledInfo != null) return false
        if (status != BuildStatus.SUCCESS && status != BuildStatus.FAILURE) return false
        if (startDateTime == null || finishDateTime == null) return false
        return hasTargetBranch()
    }

    private fun Build.hasTargetBranch(): Boolean {
        return getResultingParameters()
            .any { parameter -> parameter.name == TARGET_BRANCH_PARAMETER && !parameter.value.isNullOrBlank() }
    }

    private fun Build.duration(): Duration = Duration.between(startDateTime, finishDateTime)

    companion object {
        const val TOTAL = "total"
        const val BAD = "bad"
        const val TARGET_BRANCH_PARAMETER = "env.TARGET_BRANCH"
    }
}
