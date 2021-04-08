package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.report.HasFailedTestDeterminer
import com.avito.instrumentation.internal.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.verdict.InstrumentationTestsTaskVerdict
import com.avito.report.StubReportViewer
import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.createStubInstance
import com.github.salomonbrys.kotson.fromJson
import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

public class WriteTaskVerdictActionTest {

    private val gson = InstrumentationTestsActionFactory.gson
    private val reportViewer = StubReportViewer().apply {
        byReportCoordinatesUrl = "https://byreportcoordinates".toHttpUrl()
        byTestName = "https://bytestname".toHttpUrl()
    }

    private val failedTest = InstrumentationTestsTaskVerdict.Test(
        testUrl = reportViewer.byTestName.toString(),
        title = "com.Test.test api22 FAILED"
    )

    private val lostTest = InstrumentationTestsTaskVerdict.Test(
        testUrl = reportViewer.byTestName.toString(),
        title = "com.avito.Test.test api22 LOST"
    )

    private fun createAction(verdict: File): WriteTaskVerdictAction {
        return WriteTaskVerdictAction(
            coordinates = ReportCoordinates.createStubInstance(),
            verdictDestination = verdict,
            reportViewer = reportViewer,
            gson
        )
    }

    @Test
    public fun `write verdict with only failed test`(@TempDir dir: File) {
        val verdict = File(dir, "verdict.json")
        val action = createAction(verdict)
        action.action(
            TestRunResult(
                reportedTests = emptyList(),
                failed = HasFailedTestDeterminer.Result.Failed(
                    failed = listOf(SimpleRunTest.createStubInstance())
                ),
                notReported = HasNotReportedTestsDeterminer.Result.AllTestsReported
            )
        )

        val expected = InstrumentationTestsTaskVerdict(
            title = "Failed. There are 1 unsuppressed failed tests",
            reportUrl = reportViewer.byReportCoordinatesUrl.toString(),
            causeFailureTests = setOf(failedTest)
        )

        val actual = gson.fromJson<InstrumentationTestsTaskVerdict>(verdict.reader())

        assertThat(actual)
            .isEqualTo(expected)
    }

    @Test
    public fun `write verdict with only lost test`(@TempDir dir: File) {
        val verdict = File(dir, "verdict.json")
        val action = createAction(verdict)
        action.action(
            TestRunResult(
                reportedTests = emptyList(),
                failed = HasFailedTestDeterminer.Result.NoFailed,
                notReported = HasNotReportedTestsDeterminer.Result.HasNotReportedTests(
                    lostTests = listOf(AndroidTest.Lost.createStubInstance())
                )
            )
        )

        val expected = InstrumentationTestsTaskVerdict(
            title = "Failed. There are 1 not reported tests.",
            reportUrl = reportViewer.byReportCoordinatesUrl.toString(),
            causeFailureTests = setOf(lostTest)
        )

        val actual = gson.fromJson<InstrumentationTestsTaskVerdict>(verdict.reader())

        assertThat(actual)
            .isEqualTo(expected)
    }

    @Test
    public fun `write verdict with lost and failed tests`(@TempDir dir: File) {
        val verdict = File(dir, "verdict.json")
        val action = createAction(verdict)
        action.action(
            TestRunResult(
                reportedTests = emptyList(),
                failed = HasFailedTestDeterminer.Result.Failed(
                    failed = listOf(SimpleRunTest.createStubInstance())
                ),
                notReported = HasNotReportedTestsDeterminer.Result.HasNotReportedTests(
                    lostTests = listOf(AndroidTest.Lost.createStubInstance())
                )
            )
        )

        val expected = InstrumentationTestsTaskVerdict(
            title = "Failed. There are 1 unsuppressed failed tests. " +
                "\nFailed. There are 1 not reported tests.",
            reportUrl = reportViewer.byReportCoordinatesUrl.toString(),
            causeFailureTests = setOf(failedTest, lostTest)
        )

        val actual = gson.fromJson<InstrumentationTestsTaskVerdict>(verdict.reader())

        assertThat(actual)
            .isEqualTo(expected)
    }
}
