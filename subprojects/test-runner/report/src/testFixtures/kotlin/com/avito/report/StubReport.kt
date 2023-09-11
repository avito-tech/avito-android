package com.avito.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestAttempt
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStatus
import com.avito.test.model.TestCase

class StubReport : Report {

    var reportedSkippedTests: List<Pair<TestStaticData, String>>? = null

    var reportedMissingTests: Collection<AndroidTest.Lost>? = null

    var previousRunResults: Result<Map<TestCase, TestStatus>> = Result.Success(emptyMap())

    var getTests: List<AndroidTest> = emptyList()

    var reportIdToRunResults: Result<Map<String, Map<TestCase, TestStatus>>> = Result.Success(emptyMap())

    override val reportLinksGenerator: ReportLinksGenerator
        get() = NoOpReportLinksGenerator()

    override val testSuiteNameProvider: TestSuiteNameProvider
        get() = NoOpTestSuiteNameProvider()

    override fun addTest(testAttempt: TestAttempt) {
        TODO("Not yet implemented")
    }

    override fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        reportedSkippedTests = skippedTests
    }

    override fun reportLostTests(notReportedTests: Collection<AndroidTest.Lost>) {
        reportedMissingTests = notReportedTests
    }

    override fun getTestResults(): Collection<AndroidTest> {
        return getTests
    }

    override fun getPreviousRunsResults(): Result<Map<TestCase, TestStatus>> {
        return previousRunResults
    }

    override fun getRunResultsById(id: String): Result<Map<TestCase, TestStatus>> {
        return reportIdToRunResults.map {
            it.getOrElse(id) {
                throw NoSuchElementException("No stub for report id: $id")
            }
        }
    }
}
