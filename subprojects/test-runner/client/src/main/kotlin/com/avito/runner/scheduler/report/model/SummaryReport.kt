package com.avito.runner.scheduler.report.model

import com.avito.runner.model.TestCaseRun

internal class SummaryReport(
    val reports: List<TestCaseRequestMatchingReport>,
    val durationMilliseconds: Long
) {

    val matchedCount: Int by lazy {
        reports.count { it.result is TestCaseRequestMatchingReport.Result.Matched }
    }

    val mismatched: Int by lazy {
        reports.count { it.result is TestCaseRequestMatchingReport.Result.Mismatched }
    }

    val ignoredCount: Int by lazy {
        reports.count { it.result is TestCaseRequestMatchingReport.Result.Ignored }
    }

    val successRunsCount: Int by lazy {
        reports
            .flatMap { it.runs }
            .count { it.testCaseRun.result is TestCaseRun.Result.Passed }
    }

    val failedRunsCount: Int by lazy {
        reports
            .flatMap { it.runs }
            .count { it.testCaseRun.result is TestCaseRun.Result.Failed }
    }

    val ignoredRunsCount: Int by lazy {
        reports
            .flatMap { it.runs }
            .count { it.testCaseRun.result is TestCaseRun.Result.Ignored }
    }
}
