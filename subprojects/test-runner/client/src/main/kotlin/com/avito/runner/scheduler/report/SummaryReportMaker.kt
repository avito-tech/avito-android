package com.avito.runner.scheduler.report

import com.avito.runner.model.TestCaseRun
import com.avito.runner.scheduler.report.model.SummaryReport
import com.avito.runner.scheduler.report.model.TestCaseRequestMatchingReport
import com.avito.runner.scheduler.runner.model.TestRunResult

internal interface SummaryReportMaker {

    fun make(
        results: List<TestRunResult>,
        startTimeMilliseconds: Long
    ): SummaryReport
}

internal class SummaryReportMakerImpl : SummaryReportMaker {

    override fun make(
        results: List<TestRunResult>,
        startTimeMilliseconds: Long
    ): SummaryReport {

        val reports = results.map { (request, results) ->
            val failedRuns = results.count { it.testCaseRun.result is TestCaseRun.Result.Failed }
            val ignoredRuns = results.count { it.testCaseRun.result is TestCaseRun.Result.Ignored }
            val successRuns = results.count { it.testCaseRun.result is TestCaseRun.Result.Passed }

            val testResult: TestCaseRequestMatchingReport.Result = when {
                ignoredRuns > 0 ->
                    TestCaseRequestMatchingReport.Result.Ignored

                successRuns >= request.scheduling.minimumSuccessCount
                    && failedRuns >= request.scheduling.minimumFailedCount ->
                    TestCaseRequestMatchingReport.Result.Matched

                else ->
                    TestCaseRequestMatchingReport.Result.Mismatched
            }

            TestCaseRequestMatchingReport(
                request = request,
                runs = results,
                result = testResult
            )
        }

        return SummaryReport(
            reports = reports,
            durationMilliseconds = System.currentTimeMillis() - startTimeMilliseconds
        )
    }
}
