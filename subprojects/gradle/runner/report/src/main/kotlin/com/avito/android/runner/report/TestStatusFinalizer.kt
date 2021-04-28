package com.avito.android.runner.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.Kind
import com.avito.report.model.Stability
import com.avito.report.model.Status

/**
 * It duplicates logic from
 * com.avito.report.internal.ReportsFetchApiImpl.deserializeStatus
 * com.avito.report.internal.ReportsFetchApiImpl.determineStability and etc.
 */
public interface TestStatusFinalizer {

    public data class TestStatus(
        val status: Status,
        val stability: Stability,
        val startTime: Long,
        val endTime: Long,
        val skippedReason: String?,
        val isFinished: Boolean,
        val lastAttemptDurationInSeconds: Int
    )

    public fun getTestFinalStatus(testAttempts: List<AndroidTest>): TestStatus

    public companion object {

        public fun create(): TestStatusFinalizer = Impl()
    }

    private class Impl : TestStatusFinalizer {

        override fun getTestFinalStatus(
            testAttempts: List<AndroidTest>
        ): TestStatus {

            val startTime = getStartTime(testAttempts)
            val endTime = getEndTime(testAttempts)
            return TestStatus(
                status = getStatus(testAttempts),
                stability = getStability(testAttempts),
                startTime = startTime,
                endTime = endTime,
                skippedReason = getSkippedReason(testAttempts),
                isFinished = getFinished(testAttempts),
                lastAttemptDurationInSeconds = (endTime - startTime).toInt()
            )
        }

        private fun getFinished(testAttempts: List<AndroidTest>): Boolean {
            return when (testAttempts.last()) {
                is AndroidTest.Skipped -> false
                is AndroidTest.Lost -> false
                else -> true
            }
        }

        private fun getSkippedReason(testAttempts: List<AndroidTest>): String? {
            return when (val attempt = testAttempts.last()) {
                is AndroidTest.Skipped -> attempt.skipReason
                else -> null
            }
        }

        private fun getEndTime(testAttempts: List<AndroidTest>): Long {
            return when (val attempt = testAttempts.last()) {
                is AndroidTest.Completed -> attempt.endTime
                is AndroidTest.Lost -> attempt.lastSignalTime
                else -> 0L
            }
        }

        private fun getStartTime(testAttempts: List<AndroidTest>): Long {
            return when (val attempt = testAttempts.last()) {
                is AndroidTest.Completed -> attempt.startTime
                is AndroidTest.Lost -> attempt.startTime
                else -> 0L
            }
        }

        private fun getStability(testAttempts: List<AndroidTest>): Stability {
            val attemptsCount = testAttempts.size
            val successCount =
                testAttempts.filterIsInstance<AndroidTest.Completed>().filter { it.incident == null }.count()
            val skippedCount = testAttempts.filterIsInstance<AndroidTest.Skipped>().size
            return when {
                successCount + skippedCount == attemptsCount -> Stability.Stable(attemptsCount, successCount)
                successCount + skippedCount == 0 -> Stability.Failing(attemptsCount)
                else -> Stability.Flaky(attemptsCount, successCount + skippedCount)
            }
        }

        private fun getStatus(testAttempts: List<AndroidTest>): Status {
            return when (val attempt = testAttempts.last()) {
                is AndroidTest.Lost -> Status.Lost
                is AndroidTest.Skipped -> Status.Skipped(attempt.skipReason)
                is AndroidTest.Completed -> {
                    val incident = attempt.incident
                    if (incident == null) {
                        if (attempt.kind == Kind.MANUAL) {
                            Status.Manual
                        } else {
                            Status.Success
                        }
                    } else {
                        // TODO remove stubs
                        Status.Failure("Stub", incident.chain.lastOrNull()?.message ?: "Stub")
                    }
                }
            }
        }
    }
}
