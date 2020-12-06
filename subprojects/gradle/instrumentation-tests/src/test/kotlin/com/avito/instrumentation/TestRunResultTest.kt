package com.avito.instrumentation

import com.avito.instrumentation.report.HasFailedTestDeterminer
import com.avito.instrumentation.report.HasNotReportedTestsDeterminer
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test

internal class TestRunResultTest {

    private val gson = GsonBuilder().setPrettyPrinting().create()

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
                TestRunResult.Verdict.Failure::class.java
            )
        }

        assertThat(gson.toJson(verdict))
            .isEqualTo(
                """
{
  "message": "Failed. There are 1 unsuppressed failed tests",
  "prettifiedDetails": {
    "lostTests": [],
    "failedTests": [
      {
        "name": "com.Test.test",
        "devices": [
          "api22"
        ]
      }
    ]
  }
}""".trimIndent()
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

        assertThat(verdict).apply {
            isInstanceOf(
                TestRunResult.Verdict.Failure::class.java
            )
        }

        assertThat(gson.toJson(verdict))
            .isEqualTo(
                """
{
  "message": "Failed. There are 2 unsuppressed failed tests",
  "prettifiedDetails": {
    "lostTests": [],
    "failedTests": [
      {
        "name": "com.Test.test",
        "devices": [
          "api21",
          "api27"
        ]
      }
    ]
  }
}""".trimIndent()
            )
    }
}
