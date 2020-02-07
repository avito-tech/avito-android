package com.avito.instrumentation.report

import com.avito.instrumentation.suite.filter.TestRunFilter
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData
import okhttp3.HttpUrl

class FakeReport : Report {

    var reportedSkippedTests: List<Pair<TestStaticData, TestRunFilter.Verdict.Skip>>? = null
    var reportedMissingTests: Collection<AndroidTest.Lost>? = null
    var reportId: String? = null

    override fun tryCreate(apiUrl: String, gitBranch: String, gitCommit: String) {
    }

    override fun tryGetId(): String? = reportId

    override fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, TestRunFilter.Verdict.Skip>>) {
        reportedSkippedTests = skippedTests
    }

    override fun sendLostTests(lostTests: List<AndroidTest.Lost>) {
        reportedMissingTests = lostTests
    }

    override fun sendCompletedTest(completedTest: AndroidTest.Completed) {
        TODO("not implemented")
    }

    override fun finish(isFullTestSuite: Boolean, reportViewerUrl: HttpUrl) {
    }
}
