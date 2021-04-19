package com.avito.android.runner.report.internal

import com.avito.android.Result
import com.avito.android.runner.report.Report
import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestName
import com.avito.report.model.TestStaticData

internal class ReportImpl(
    private val inMemoryReport: InMemoryReport,
    private val avitoReport: AvitoReport?, // todo should be generic reports from config
    private val useInMemoryReport: Boolean
) : Report {

    override fun addTest(test: AndroidTest) {
        inMemoryReport.addTest(test)

        if (useInMemoryReport) {
            avitoReport?.addTest(test)
        } else {
            avitoReport!!.addTest(test)
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

    override fun getTests(initialSuiteFilter: List<TestName>): Result<List<SimpleRunTest>> {
        return if (useInMemoryReport) {
            inMemoryReport.getTests()
        } else {
            avitoReport!!.getTests(initialSuiteFilter)
        }
    }
}
