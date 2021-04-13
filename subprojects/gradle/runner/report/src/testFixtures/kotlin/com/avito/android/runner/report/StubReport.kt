package com.avito.android.runner.report

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.report.model.createStubInstance
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

    public var getTestsResult: Result<List<SimpleRunTest>> = Result.Success(emptyList())

    override fun addTest(test: AndroidTest) {
        TODO("not implemented")
    }

    override fun tryCreate(testHost: String, gitBranch: String, gitCommit: String) {
    }

    override fun tryGetId(): String? = reportId

    override fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        reportedSkippedTests = skippedTests
    }

    override fun sendLostTests(lostTests: List<AndroidTest.Lost>) {
        reportedMissingTests = lostTests
    }

    override fun finish() {
    }

    override fun getTests(): Result<List<SimpleRunTest>> {
        return getTestsResult
    }

    override fun markAsSuccessful(testRunId: String, author: String, comment: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun getCrossDeviceTestData(): Result<CrossDeviceSuite> {
        TODO("Not yet implemented")
    }
}
