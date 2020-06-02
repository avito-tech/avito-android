package com.avito.instrumentation.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import org.funktionale.tries.Try

class FakeReport : Report {

    var reportedSkippedTests: List<Pair<TestStaticData, String>>? = null
    var reportedMissingTests: Collection<AndroidTest.Lost>? = null
    var reportId: String? = null

    override fun tryCreate(apiUrl: String, gitBranch: String, gitCommit: String) {
    }

    override fun tryGetId(): String? = reportId

    override fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        reportedSkippedTests = skippedTests
    }

    override fun sendLostTests(lostTests: List<AndroidTest.Lost>) {
        reportedMissingTests = lostTests
    }

    override fun sendCompletedTest(completedTest: AndroidTest.Completed) {
        TODO("not implemented")
    }

    override fun finish(isFullTestSuite: Boolean) {
    }

    override fun getTests(): Try<List<SimpleRunTest>> {
        TODO("Not yet implemented")
    }

    override fun markAsSuccessful(testRunId: String, author: String, comment: String): Try<Unit> {
        TODO("Not yet implemented")
    }

    override fun getCrossDeviceTestData(): Try<CrossDeviceSuite> {
        TODO("Not yet implemented")
    }
}
