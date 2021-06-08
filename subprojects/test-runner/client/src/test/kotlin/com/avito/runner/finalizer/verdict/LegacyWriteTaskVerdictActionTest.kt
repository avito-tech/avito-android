package com.avito.runner.finalizer.verdict

import com.avito.report.NoOpReportLinkGenerator
import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.createStubInstance
import com.avito.runner.finalizer.TestRunResult
import com.avito.runner.finalizer.action.LegacyWriteTaskVerdictAction
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

public class LegacyWriteTaskVerdictActionTest {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
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
            problemTests = setOf(failedTest)
        )

        val actual = gson.fromJson(verdict.reader(), InstrumentationTestsTaskVerdict::class.java)

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
            problemTests = setOf(lostTest)
        )

        val actual = gson.fromJson(verdict.reader(), InstrumentationTestsTaskVerdict::class.java)

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
            problemTests = setOf(failedTest, lostTest)
        )

        val actual = gson.fromJson(verdict.reader(), InstrumentationTestsTaskVerdict::class.java)

        assertThat(actual).isEqualTo(expected)
    }

    private fun createAction(verdict: File): LegacyWriteTaskVerdictAction {
        return LegacyWriteTaskVerdictAction(
            verdictDestination = verdict,
            reportLinkGenerator = NoOpReportLinkGenerator(
                reportLink = byReportCoordinatesUrl,
                testLink = byTestName
            )
        )
    }

    private fun createVerdictDeterminer(): LegacyVerdictDeterminer {
        return LegacyVerdictDeterminerFactory.create()
    }
}
