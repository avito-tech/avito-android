package com.avito.instrumentation.report

import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Stability
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test

internal class FlakyTestInfoTest {

    private val flakyTestInfo = FlakyTestInfo()

    @Test
    fun `test info summarized`() {
        flakyTestInfo.addReport(
            report = Try.Success(
                listOf(
                    SimpleRunTest.createStubInstance(
                        name = "com.avito.Test.test",
                        stability = Stability.Flaky(attemptsCount = 3, successCount = 1),
                        lastAttemptDurationInSeconds = 22
                    )
                )
            )
        )

        flakyTestInfo.addReport(
            report = Try.Success(
                listOf(
                    SimpleRunTest.createStubInstance(
                        name = "com.avito.Test.test",
                        stability = Stability.Flaky(attemptsCount = 2, successCount = 1),
                        lastAttemptDurationInSeconds = 10
                    )
                )
            )
        )

        val info = flakyTestInfo.getInfo().single()
        assertThat(info.attempts).isEqualTo(5)
        assertThat(info.wastedTimeEstimateInSec).isEqualTo(86)
    }
}
