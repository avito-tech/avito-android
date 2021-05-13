package com.avito.android.runner.report.internal

import com.avito.android.runner.report.Report
import com.avito.android.runner.report.TestAttempt
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData

internal class ReportImpl(
    private val inMemoryReport: InMemoryReport,
    private val avitoReport: AvitoReport?, // todo should be generic reports from config
    private val useInMemoryReport: Boolean
) : Report {

    override fun addTest(testAttempt: TestAttempt) {
        inMemoryReport.addTest(testAttempt)

        if (useInMemoryReport) {
            avitoReport?.addTest(testAttempt)
        } else {
            avitoReport!!.addTest(testAttempt)
        }
    }

    override fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        inMemoryReport.addSkippedTests(skippedTests)

        if (useInMemoryReport) {
            avitoReport?.addSkippedTests(skippedTests)
        } else {
            avitoReport!!.addSkippedTests(skippedTests)
        }
    }

    override fun getTestResults(): Collection<AndroidTest> {
        return if (useInMemoryReport) {
            inMemoryReport.getTestResults()
        } else {
            // will be fetched in [LegacyFinalizer]
            emptyList()
        }
    }
}
