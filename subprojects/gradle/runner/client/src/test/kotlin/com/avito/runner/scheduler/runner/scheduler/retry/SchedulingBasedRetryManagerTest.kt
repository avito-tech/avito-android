package com.avito.runner.scheduler.runner.scheduler.retry

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.test.Is
import com.avito.runner.test.generateDeviceTestCaseRun
import com.avito.runner.test.generateTestCaseRun
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class SchedulingBasedRetryManagerTest {

    @Test
    fun `retry not required - run is failed and when retry quota is 0`() {
        val manager = provideSchedulingBasedRetryManager(quota = 0, minimalPassedCount = 1)

        assertThat(
            manager.retryCount(
                history = listOf(
                    FAILED_RUN
                )
            ),
            Is(0)
        )
    }

    @Test
    fun `retry not required - run is passed and minimal success count is 1`() {
        val manager = provideSchedulingBasedRetryManager(quota = 0, minimalPassedCount = 1)

        assertThat(
            manager.retryCount(
                history = listOf(
                    SUCCESS_RUN
                )
            ),
            Is(0)
        )
    }

    @Test
    fun `retry is required - run is failed and when retry quota is 1`() {
        val manager = provideSchedulingBasedRetryManager(quota = 1, minimalPassedCount = 1)

        assertThat(
            manager.retryCount(
                history = listOf(
                    FAILED_RUN
                )
            ),
            Is(1)
        )
    }

    @Test
    fun `retry is required - run is passed and when retry quota is 1 and minimal success count is 2`() {
        val manager = provideSchedulingBasedRetryManager(quota = 1, minimalPassedCount = 2)

        assertThat(
            manager.retryCount(
                history = listOf(
                    SUCCESS_RUN
                )
            ),
            Is(1)
        )
    }

    @Test
    fun `retry is not required - remaining retries is not enough for passing minimalPassedCount`() {
        val manager = provideSchedulingBasedRetryManager(quota = 4, minimalPassedCount = 3)

        assertThat(
            manager.retryCount(
                history = listOf(
                    FAILED_RUN,
                    FAILED_RUN,
                    FAILED_RUN
                )
            ),
            Is(0)
        )
    }

    @Test
    fun `required 2 retries - run is passed and when retry quota is 4 and minimal success count is 3`() {
        val manager = provideSchedulingBasedRetryManager(quota = 4, minimalPassedCount = 3)

        assertThat(
            manager.retryCount(
                history = listOf(
                    SUCCESS_RUN
                )
            ),
            Is(2)
        )
    }

    @Test
    fun `0 remaining retries - remaining retries is not enough for passing minimalPassedCount`() {
        val manager = provideSchedulingBasedRetryManager(quota = 2, minimalPassedCount = 4, minimalFailedCount = 0)

        assertThat(
            manager.retryCount(
                history = listOf(
                    FAILED_RUN,
                    FAILED_RUN,
                    FAILED_RUN
                )
            ),
            Is(0)
        )
    }

    @Test
    fun `0 remaining retries - remaining retries is not enough for passing minimalFailedCount`() {
        val manager = provideSchedulingBasedRetryManager(quota = 2, minimalPassedCount = 0, minimalFailedCount = 4)

        assertThat(
            manager.retryCount(
                history = listOf(
                    SUCCESS_RUN,
                    SUCCESS_RUN,
                    SUCCESS_RUN
                )
            ),
            Is(0)
        )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `0 remaining retries - remaining retries is not enough for passing minimalFailedCount and minimalPassedCount`() {
        val manager = provideSchedulingBasedRetryManager(quota = 3, minimalPassedCount = 2, minimalFailedCount = 2)

        assertThat(
            manager.retryCount(
                history = listOf(
                    SUCCESS_RUN,
                    SUCCESS_RUN,
                    SUCCESS_RUN
                )
            ),
            Is(0)
        )
    }

    @Test
    fun `2 remaining retries - when required 2 passed and 2 failed results after 1 passed and 1 failed`() {
        val manager = provideSchedulingBasedRetryManager(quota = 3, minimalPassedCount = 2, minimalFailedCount = 2)

        assertThat(
            manager.retryCount(
                history = listOf(
                    SUCCESS_RUN,
                    FAILED_RUN
                )
            ),
            Is(2)
        )
    }

    @Test
    fun `10 remaining retries - when required 10 passed and 0 failed results`() {
        val manager = provideSchedulingBasedRetryManager(quota = 19, minimalPassedCount = 10, minimalFailedCount = 0)

        assertThat(
            manager.retryCount(
                history = listOf()
            ),
            Is(10)
        )
    }

    @Test
    fun `11 remaining retries - when required 10 passed and 10 failed results`() {
        val manager = provideSchedulingBasedRetryManager(quota = 19, minimalPassedCount = 10, minimalFailedCount = 10)

        assertThat(
            manager.retryCount(
                history = listOf()
            ),
            Is(11)
        )
    }

    private fun provideSchedulingBasedRetryManager(
        quota: Int,
        minimalPassedCount: Int,
        minimalFailedCount: Int = 0
    ) =
        SchedulingBasedRetryManager(
            scheduling = TestRunRequest.Scheduling(
                retryCount = quota,
                minimumSuccessCount = minimalPassedCount,
                minimumFailedCount = minimalFailedCount
            )
        )

    companion object {
        private val FAILED_RUN = generateDeviceTestCaseRun(
            testCaseRun = generateTestCaseRun(
                result = TestCaseRun.Result.Failed.InRun("failed")
            )
        )

        private val SUCCESS_RUN = generateDeviceTestCaseRun(
            testCaseRun = generateTestCaseRun(
                result = TestCaseRun.Result.Passed
            )
        )
    }
}
