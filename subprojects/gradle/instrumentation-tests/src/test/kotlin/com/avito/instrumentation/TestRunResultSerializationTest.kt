package com.avito.instrumentation

import com.avito.instrumentation.internal.InstrumentationTestsActionFactory.Companion.gson
import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.TestRunResult.Verdict.Failure.Details
import com.github.salomonbrys.kotson.fromJson
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TestRunResultSerializationTest {

    @Test
    fun `verdict is success`() {
        val expected: TestRunResult.Verdict = TestRunResult.Verdict.Success("Success")
        val json = gson.toJson(expected)
        val actual = gson.fromJson<TestRunResult.Verdict>(json)

        assertThat(actual)
            .isEqualTo(expected)
    }

    @Test
    fun `verdict is failure`() {
        val expected: TestRunResult.Verdict = TestRunResult.Verdict.Failure(
            message = "Failure",
            prettifiedDetails = Details(
                lostTests = setOf(
                    Details.Test(
                        name = "LostTest",
                        devices = setOf("22")
                    )
                ),
                failedTests = setOf(
                    Details.Test(
                        name = "FailedTest",
                        devices = setOf("29")
                    )
                ),
            ),
            cause = null
        )
        val json = gson.toJson(expected)
        val actual = gson.fromJson<TestRunResult.Verdict>(json)

        assertThat(actual)
            .isEqualTo(expected)
    }
}
