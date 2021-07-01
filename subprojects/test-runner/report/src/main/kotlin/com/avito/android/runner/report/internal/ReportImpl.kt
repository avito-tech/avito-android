package com.avito.android.runner.report.internal

import com.avito.android.runner.report.Report
import com.avito.android.runner.report.TestAttempt
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

internal class ReportImpl(
    private val inMemoryReport: InMemoryReport,
    private val avitoReport: AvitoReport?
) : Report {

    override fun addTest(testAttempt: TestAttempt) {
        inMemoryReport.addTest(testAttempt)

        avitoReport?.addTest(testAttempt)
    }

    override fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        inMemoryReport.addSkippedTests(skippedTests)

        avitoReport?.addSkippedTests(skippedTests)
    }

    override fun getTestResults(): Collection<AndroidTest> {
        return inMemoryReport.getTestResults()
    }
}
