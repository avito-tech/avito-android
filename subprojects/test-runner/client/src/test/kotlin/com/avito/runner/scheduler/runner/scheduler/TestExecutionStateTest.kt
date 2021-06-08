package com.avito.runner.scheduler.runner.scheduler

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.scheduler.retry.RetryManager
import com.avito.runner.scheduler.runner.scheduler.retry.SchedulingBasedRetryManager
import com.avito.runner.scheduler.util.generateTestRunRequest
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.test.generateDeviceTestCaseRun
import com.avito.runner.test.generateTestCaseRun
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test

class TestExecutionStateTest {

    @Test
    fun `verdict from state must be SendResult after 1 success run when minimal passed count is 1`() {
        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 5,
                minimalPassedCount = 1
            )
        )

        val runs = listOf(
            createPassedTestCaseRun()
        )

        assertWithMessage("Verdict must be SendResult")
            .that(state.verdict(incomingTestCaseRun = runs[0]))
            .isEqualTo(
                TestExecutionState.Verdict.SendResult(
                    runs
                )
            )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `verdict from state must be ReRun with intention with retry number instrumentation param 1 after 1 failed run when retry quota is 2`() {
        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 2,
                minimalPassedCount = 1
            )
        )

        val runs = listOf(
            createFailedTestCaseRun()
        )

        state.verdict(incomingTestCaseRun = null)

        val verdict = state.verdict(incomingTestCaseRun = runs[0])

        assertWithMessage("Verdict must be Run")
            .that(verdict)
            .isInstanceOf<TestExecutionState.Verdict.Run>()
        assertWithMessage("Should have 1")
            .that((verdict as TestExecutionState.Verdict.Run).intentions)
            .hasSize(1)
    }

    @Test
    fun `verdict from state must be Run with 10 intentions when retry quota is 20 and minimal passed count is 10`() {
        val minimalPassedCount = 10
        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 20,
                minimalPassedCount = minimalPassedCount
            )
        )

        with(state.verdict(incomingTestCaseRun = null)) {
            assertWithMessage("Verdict must be Run")
                .that(this)
                .isInstanceOf<TestExecutionState.Verdict.Run>()
            assertWithMessage("Should have $minimalPassedCount")
                .that((this as TestExecutionState.Verdict.Run).intentions)
                .hasSize(minimalPassedCount)
        }
    }

    @Suppress("MaxLineLength")
    @Test
    fun `verdict from state must be Run with 1 intentions 2 times when retry quota is 20 and minimal passed count is 10 and 2 runs failed`() {
        val minimalPassedCount = 10
        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 20,
                minimalPassedCount = minimalPassedCount
            )
        )

        state.verdict(incomingTestCaseRun = null)

        val failedRuns = listOf(
            createFailedTestCaseRun(),
            createFailedTestCaseRun()
        )

        failedRuns.forEachIndexed { _, incomingTestCaseRun ->
            val verdict = state.verdict(incomingTestCaseRun = incomingTestCaseRun)

            assertWithMessage("Verdict must be Run")
                .that(verdict)
                .isInstanceOf<TestExecutionState.Verdict.Run>()
            assertWithMessage("Should have 1")
                .that((verdict as TestExecutionState.Verdict.Run).intentions)
                .hasSize(1)
        }
    }

    @Suppress("MaxLineLength")
    @Test
    fun `verdict from state must be Run with 0 intentions 2 times when retry quota is 20 and minimal passed count is 10 and 2 runs passed`() {
        val minimalPassedCount = 10
        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 20,
                minimalPassedCount = minimalPassedCount
            )
        )

        val initialVerdict = state.verdict(incomingTestCaseRun = null)

        assertWithMessage("Initial verdict must has MultipleRun type")
            .that(initialVerdict)
            .isInstanceOf<TestExecutionState.Verdict.Run>()
        assertWithMessage("Initial verdict must has 10 runs")
            .that((initialVerdict as TestExecutionState.Verdict.Run).intentions)
            .hasSize(10)

        val successfulRuns =
            listOf(
                createPassedTestCaseRun(),
                createPassedTestCaseRun()
            )

        successfulRuns.forEachIndexed { _, incomingTestCaseRun ->
            val verdict = state.verdict(incomingTestCaseRun = incomingTestCaseRun)

            assertWithMessage("Verdict must be DoNothing")
                .that(verdict)
                .isInstanceOf<TestExecutionState.Verdict.DoNothing>()
        }
    }

    @Suppress("MaxLineLength")
    @Test
    fun `verdict from state must be Run with 0 intentions 2 times when retry quota is 20 and minimal failed count is 10 and 2 runs failed`() {
        val minimalFailedCount = 10

        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 20,
                minimalFailedCount = minimalFailedCount,
                minimalPassedCount = 0
            )
        )

        val initialVerdict = state.verdict(incomingTestCaseRun = null)

        assertWithMessage("Initial verdict must has MultipleRun type")
            .that(initialVerdict)
            .isInstanceOf<TestExecutionState.Verdict.Run>()
        assertWithMessage("Initial verdict must has 10 runs")
            .that((initialVerdict as TestExecutionState.Verdict.Run).intentions)
            .hasSize(10)

        val failedRuns = listOf(
            createFailedTestCaseRun(),
            createFailedTestCaseRun()
        )

        failedRuns.forEachIndexed { _, incomingTestCaseRun ->
            val verdict = state.verdict(incomingTestCaseRun = incomingTestCaseRun)

            assertWithMessage("Verdict must be DoNothing")
                .that(verdict)
                .isInstanceOf<TestExecutionState.Verdict.DoNothing>()
        }
    }

    @Suppress("MaxLineLength")
    @Test
    fun `dont have redundant intentions when have minimalFailedCount = 10 minimalPassedCount = 10 and retryQuota = 19`() {
        val minimalSuccessCount = 10
        val minimalFailedCount = 10

        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 19,
                minimalPassedCount = minimalSuccessCount,
                minimalFailedCount = minimalFailedCount
            )
        )

        val initialVerdict = state.verdict(incomingTestCaseRun = null)

        assertWithMessage("Initial verdict must has MultipleRun type")
            .that(initialVerdict)
            .isInstanceOf<TestExecutionState.Verdict.Run>()
        assertWithMessage("Initial verdict must has 11 runs")
            .that((initialVerdict as TestExecutionState.Verdict.Run).intentions)
            .hasSize(11)

        val failedRuns = (1..10).map { createFailedTestCaseRun() }

        failedRuns.forEachIndexed { index, incomingTestCaseRun ->
            val verdict = state.verdict(incomingTestCaseRun = incomingTestCaseRun)

            assertWithMessage("Verdict must be DoNothing for event: $index in failedRuns")
                .that(verdict)
                .isInstanceOf<TestExecutionState.Verdict.DoNothing>()
        }

        val successRuns = (1..9).map { createPassedTestCaseRun() }

        successRuns.forEachIndexed { index, incomingTestCaseRun ->
            val verdict = state.verdict(incomingTestCaseRun = incomingTestCaseRun)

            assertWithMessage("Verdict must be Run for event: $index in successRuns")
                .that(verdict)
                .isInstanceOf<TestExecutionState.Verdict.Run>()

            assertWithMessage("Verdict must has only one intention for event: $index in successRuns")
                .that((verdict as TestExecutionState.Verdict.Run).intentions)
                .hasSize(1)
        }

        val verdict = state.verdict(
            incomingTestCaseRun = createPassedTestCaseRun()
        )

        assertWithMessage("Last (20th) verdict must be as SendResult")
            .that(verdict)
            .isInstanceOf<TestExecutionState.Verdict.SendResult>()

        val results =
            (verdict as TestExecutionState.Verdict.SendResult).results

        assertWithMessage("SendResult verdict must has first 10 failed test events")
            .that(results.slice(0..9).all { it.testCaseRun.result is TestCaseRun.Result.Failed.InRun })
            .isTrue()

        assertWithMessage("SendResult verdict must has last 10 passed test events")
            .that(results.slice(10..19).all { it.testCaseRun.result is TestCaseRun.Result.Passed })
            .isTrue()
    }

    @Test
    fun `verdict must be Run with intention with executionNumber 1`() {
        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 2,
                minimalPassedCount = 1
            )
        )

        val verdict = state.verdict(incomingTestCaseRun = null)

        assertWithMessage("Verdict must be Run")
            .that(verdict)
            .isInstanceOf<TestExecutionState.Verdict.Run>()
        assertWithMessage("Should have 1")
            .that((verdict as TestExecutionState.Verdict.Run).intentions)
            .hasSize(1)
        assertWithMessage("Should be RunAction")
            .that(verdict.intentions[0].action)
            .isInstanceOf<InstrumentationTestRunAction>()
        assertWithMessage("Should be 1")
            .that(verdict.intentions[0].action.executionNumber)
            .isEqualTo(1)
    }

    @Test
    fun `verdict must be Run with intention with executionNumber 2`() {
        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 2,
                minimalPassedCount = 1
            )
        )

        val runs = listOf(
            createFailedTestCaseRun()
        )

        state.verdict(incomingTestCaseRun = null)

        val verdict = state.verdict(incomingTestCaseRun = runs[0])

        assertWithMessage("Verdict must be Run")
            .that(verdict)
            .isInstanceOf<TestExecutionState.Verdict.Run>()
        assertWithMessage("Should have 1")
            .that((verdict as TestExecutionState.Verdict.Run).intentions)
            .hasSize(1)
        assertWithMessage("Should be RunAction")
            .that(verdict.intentions[0].action)
            .isInstanceOf<InstrumentationTestRunAction>()
        assertWithMessage("Should be 2")
            .that(verdict.intentions[0].action.executionNumber)
            .isEqualTo(2)
    }

    @Test
    fun `verdict must be Run with intentions with executionNumbers 1, 2`() {
        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 3,
                minimalPassedCount = 1,
                minimalFailedCount = 1
            )
        )

        val verdict = state.verdict(incomingTestCaseRun = null)

        assertWithMessage("Verdict must be Run")
            .that(verdict)
            .isInstanceOf<TestExecutionState.Verdict.Run>()
        assertWithMessage("Should have 1")
            .that((verdict as TestExecutionState.Verdict.Run).intentions)
            .hasSize(2)
        assertWithMessage("Should be RunAction")
            .that(verdict.intentions[0].action)
            .isInstanceOf<InstrumentationTestRunAction>()
        assertWithMessage("Should be 1")
            .that(verdict.intentions[0].action.executionNumber)
            .isEqualTo(1)
        assertWithMessage("Should be RunAction")
            .that(verdict.intentions[1].action)
            .isInstanceOf<InstrumentationTestRunAction>()
        assertWithMessage("Should be 2")
            .that(verdict.intentions[1].action.executionNumber)
            .isEqualTo(2)
    }

    @Test
    fun `verdict must be Run with intention with executionNumber 5`() {
        val state = provideTestExecutionState(
            retry = provideSchedulingBasedRetryManager(
                quota = 6,
                minimalPassedCount = 2,
                minimalFailedCount = 2
            )
        )

        val runs = listOf(
            createFailedTestCaseRun()
        )
        state.verdict(incomingTestCaseRun = null)
        state.verdict(incomingTestCaseRun = runs[0])
        state.verdict(incomingTestCaseRun = runs[0])
        state.verdict(incomingTestCaseRun = runs[0])
        val verdict = state.verdict(incomingTestCaseRun = runs[0])

        assertWithMessage("Verdict must be Run")
            .that(verdict)
            .isInstanceOf<TestExecutionState.Verdict.Run>()
        assertWithMessage("Should have 1")
            .that((verdict as TestExecutionState.Verdict.Run).intentions)
            .hasSize(1)
        assertWithMessage("Should be RunAction")
            .that(verdict.intentions[0].action)
            .isInstanceOf<InstrumentationTestRunAction>()
        assertWithMessage("Should be 5")
            .that(verdict.intentions[0].action.executionNumber)
            .isEqualTo(5)
    }

    private fun createFailedTestCaseRun(): DeviceTestCaseRun {
        return generateDeviceTestCaseRun(
            testCaseRun = generateTestCaseRun(
                result = TestCaseRun.Result.Failed.InRun("")
            )
        )
    }

    private fun createPassedTestCaseRun(): DeviceTestCaseRun {
        return generateDeviceTestCaseRun(
            testCaseRun = generateTestCaseRun(
                result = TestCaseRun.Result.Passed
            )
        )
    }

    private fun provideSchedulingBasedRetryManager(
        quota: Int,
        minimalPassedCount: Int = 0,
        minimalFailedCount: Int = 0
    ) =
        SchedulingBasedRetryManager(
            scheduling = TestRunRequest.Scheduling(
                retryCount = quota,
                minimumFailedCount = minimalFailedCount,
                minimumSuccessCount = minimalPassedCount
            )
        )

    private fun provideTestExecutionState(
        request: TestRunRequest = generateTestRunRequest(),
        retry: RetryManager
    ) =
        TestExecutionStateImplementation(
            request = request,
            retryManager = retry
        )
}
