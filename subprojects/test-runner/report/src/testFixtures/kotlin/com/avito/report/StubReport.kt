package com.avito.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestAttempt
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStatus
import com.avito.test.model.TestCase

public class StubReport : Report {

    public var reportedSkippedTests: List<Pair<TestStaticData, String>>? = null

    public var reportedMissingTests: Collection<AndroidTest.Lost>? = null

    public var reportId: String? = null

    public var getTestsResult: Result<Map<TestCase, TestStatus>> = Result.Success(emptyMap())

    public var getTests: List<AndroidTest> = emptyList()

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
        return getTestsResult
    }
}
