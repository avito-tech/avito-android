package com.avito.runner.scheduler.report

import com.avito.android.Result
import com.avito.report.Report
import com.avito.report.ReportLinksGenerator
import com.avito.report.TestSuiteNameProvider
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestAttempt
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStatus
import com.avito.test.model.TestCase

internal class ReportImpl(
    private val inMemoryReport: Report,
    private val externalReportService: Report?
) : Report {

    override val reportLinksGenerator: ReportLinksGenerator
        get() = externalReportService?.reportLinksGenerator ?: inMemoryReport.reportLinksGenerator

    override val testSuiteNameProvider: TestSuiteNameProvider
        get() = externalReportService?.testSuiteNameProvider ?: inMemoryReport.testSuiteNameProvider

    override fun addTest(testAttempt: TestAttempt) {
        inMemoryReport.addTest(testAttempt)

        externalReportService?.addTest(testAttempt)
    }

    override fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        inMemoryReport.addSkippedTests(skippedTests)

        externalReportService?.addSkippedTests(skippedTests)
    }

    override fun reportLostTests(notReportedTests: Collection<AndroidTest.Lost>) {
        inMemoryReport.reportLostTests(notReportedTests)

        externalReportService?.reportLostTests(notReportedTests)
    }

    override fun getTestResults(): Collection<AndroidTest> {
        return inMemoryReport.getTestResults()
    }

    override fun getPreviousRunsResults(): Result<Map<TestCase, TestStatus>> {
        return externalReportService?.getPreviousRunsResults() ?: inMemoryReport.getPreviousRunsResults()
    }
}
