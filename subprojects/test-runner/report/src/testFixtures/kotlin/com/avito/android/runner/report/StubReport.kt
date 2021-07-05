package com.avito.android.runner.report

import com.avito.android.Result
import com.avito.android.runner.report.internal.AvitoReport
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.createStubInstance
import com.avito.test.model.TestCase
import com.avito.test.model.TestStatus
import com.avito.time.StubTimeProvider
import com.avito.time.TimeProvider

public fun Report.Companion.createStubInstance(
    reportsApi: ReportsApi,
    loggerFactory: LoggerFactory = StubLoggerFactory,
    batchSize: Int = 1,
    buildId: String = "1",
    reportCoordinates: ReportCoordinates = ReportCoordinates.createStubInstance(),
    timeProvider: TimeProvider = StubTimeProvider()
): Report = AvitoReport(
    reportsApi = reportsApi,
    loggerFactory = loggerFactory,
    batchSize = batchSize,
    buildId = buildId,
    reportCoordinates = reportCoordinates,
    timeProvider = timeProvider
)

public class StubReport : Report {

    public var reportedSkippedTests: List<Pair<TestStaticData, String>>? = null

    public var reportedMissingTests: Collection<AndroidTest.Lost>? = null

    public var reportId: String? = null

    public var getTestsResult: Result<Map<TestCase, TestStatus>> = Result.Success(emptyMap())

    public var getTests: List<AndroidTest> = emptyList()

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
