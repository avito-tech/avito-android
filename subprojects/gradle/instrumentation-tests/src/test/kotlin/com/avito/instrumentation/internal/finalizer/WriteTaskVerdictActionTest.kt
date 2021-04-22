package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.instrumentation.internal.finalizer.action.WriteTaskVerdictAction
import com.avito.instrumentation.internal.finalizer.verdict.HasFailedTestDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.InstrumentationTestsTaskVerdict
import com.avito.instrumentation.internal.finalizer.verdict.VerdictDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.VerdictDeterminerFactory
import com.avito.report.ReportLinkGenerator
import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.createStubInstance
import com.github.salomonbrys.kotson.fromJson
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

public class WriteTaskVerdictActionTest {

    private val gson = InstrumentationTestsActionFactory.gson
    private val byReportCoordinatesUrl = "https://byreportcoordinates/"
    private val byTestName = "https://bytestname/"

    private val failedTest = InstrumentationTestsTaskVerdict.Test(
        testUrl = byTestName,
        title = "com.Test.test api22 FAILED"
    )

    private val lostTest = InstrumentationTestsTaskVerdict.Test(
        testUrl = byTestName,
        title = "com.avito.Test.test api22 LOST"
    )

    @Test
    public fun `write verdict with only failed test`(@TempDir dir: File) {
        val verdict = File(dir, "verdict.json")
        val verdictDeterminer = createVerdictDeterminer()
        val action = createAction(verdict)
        val failed = HasFailedTestDeterminer.Result.Failed(
            failed = listOf(SimpleRunTest.createStubInstance())
        )
        val notReported = HasNotReportedTestsDeterminer.Result.AllTestsReported

        action.action(
            TestRunResult(
                reportedTests = emptyList(),
                failed = failed,
                notReported = notReported
            ),
            verdict = verdictDeterminer.determine(failed, notReported)
        )

        val expected = InstrumentationTestsTaskVerdict(
            title = "Failed. There are 1 unsuppressed failed tests",
            reportUrl = byReportCoordinatesUrl,
            causeFailureTests = setOf(failedTest)
        )

        val actual = gson.fromJson<InstrumentationTestsTaskVerdict>(verdict.reader())

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    public fun `write verdict with only lost test`(@TempDir dir: File) {
        val verdict = File(dir, "verdict.json")
        val action = createAction(verdict)
        val verdictDeterminer = createVerdictDeterminer()
        val failed = HasFailedTestDeterminer.Result.NoFailed
        val notReported = HasNotReportedTestsDeterminer.Result.HasNotReportedTests(
            lostTests = listOf(AndroidTest.Lost.createStubInstance())
        )

        action.action(
            TestRunResult(
                reportedTests = emptyList(),
                failed = failed,
                notReported = notReported
            ),
            verdict = verdictDeterminer.determine(failed, notReported)
        )

        val expected = InstrumentationTestsTaskVerdict(
            title = "Failed. There are 1 not reported tests.",
            reportUrl = byReportCoordinatesUrl,
            causeFailureTests = setOf(lostTest)
        )

        val actual = gson.fromJson<InstrumentationTestsTaskVerdict>(verdict.reader())

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    public fun `write verdict with lost and failed tests`(@TempDir dir: File) {
        val verdict = File(dir, "verdict.json")
        val verdictDeterminer = createVerdictDeterminer()
        val action = createAction(verdict)
        val failed = HasFailedTestDeterminer.Result.Failed(
            failed = listOf(SimpleRunTest.createStubInstance())
        )
        val notReported = HasNotReportedTestsDeterminer.Result.HasNotReportedTests(
            lostTests = listOf(AndroidTest.Lost.createStubInstance())
        )
        action.action(
            TestRunResult(
                reportedTests = emptyList(),
                failed = failed,
                notReported = notReported
            ),
            verdict = verdictDeterminer.determine(failed, notReported)
        )

        val expected = InstrumentationTestsTaskVerdict(
            title = "Failed. There are 1 unsuppressed failed tests. " +
                "\nFailed. There are 1 not reported tests.",
            reportUrl = byReportCoordinatesUrl,
            causeFailureTests = setOf(failedTest, lostTest)
        )

        val actual = gson.fromJson<InstrumentationTestsTaskVerdict>(verdict.reader())

        assertThat(actual).isEqualTo(expected)
    }

    private fun createAction(verdict: File): WriteTaskVerdictAction {
        return WriteTaskVerdictAction(
            verdictDestination = verdict,
            gson = gson,
            reportLinkGenerator = ReportLinkGenerator.Stub(
                reportLink = byReportCoordinatesUrl,
                testLink = byTestName
            )
        )
    }

    private fun createVerdictDeterminer(): VerdictDeterminer {
        return VerdictDeterminerFactory.create()
    }
}
