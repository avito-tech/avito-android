package com.avito.instrumentation

import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.TestRunResult.Verdict.Failure
import com.avito.instrumentation.internal.report.HasFailedTestDeterminer
import com.avito.instrumentation.internal.report.HasNotReportedTestsDeterminer
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestName
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestRunResultTest {

    @Test
    fun `has 1 unsuppresed test`() {
        val verdict = TestRunResult(
            reportedTests = emptyList(),
            failed = HasFailedTestDeterminer.Result.Failed(
                failed = listOf(
                    SimpleRunTest.createStubInstance()
                )
            ),
            notReported = HasNotReportedTestsDeterminer.Result.AllTestsReported
        ).verdict

        assertThat(verdict).apply {
            isInstanceOf(
                Failure::class.java
            )
        }

        verdict.assertVerdictFailure(
            Failure.Details.Test(
                name = TestName("com.Test", "test"),
                devices = setOf("api22")
            )
        )
    }

    @Test
    fun `has 1 unsuppresed test with 2 apis`() {
        val verdict = TestRunResult(
            reportedTests = emptyList(),
            failed = HasFailedTestDeterminer.Result.Failed(
                failed = listOf(
                    SimpleRunTest.createStubInstance(deviceName = "api21"),
                    SimpleRunTest.createStubInstance(deviceName = "api27")
                )
            ),
            notReported = HasNotReportedTestsDeterminer.Result.AllTestsReported
        ).verdict

        verdict.assertVerdictFailure(
            Failure.Details.Test(
                name = TestName("com.Test", "test"),
                devices = setOf("api21", "api27")
            )
        )
    }

    private fun TestRunResult.Verdict.assertVerdictFailure(
        failedTest: Failure.Details.Test
    ) {
        assertThat(this).apply {
            isInstanceOf(
                Failure::class.java
            )
        }

        with(this as Failure) {
            assertThat(
                prettifiedDetails.lostTests
            ).isEmpty()

            assertThat(
                prettifiedDetails.failedTests
            ).hasSize(1)

            assertThat(
                prettifiedDetails.failedTests.first()
            ).isEqualTo(
                failedTest
            )
        }
    }
}
