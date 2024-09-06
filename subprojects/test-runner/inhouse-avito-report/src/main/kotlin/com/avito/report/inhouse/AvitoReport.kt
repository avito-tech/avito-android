package com.avito.report.inhouse

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.Report
import com.avito.report.ReportLinksGenerator
import com.avito.report.TestSuiteNameProvider
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestAttempt
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStatus
import com.avito.reportviewer.ReportViewerLinksGeneratorImpl
import com.avito.reportviewer.ReportViewerQuery
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.SimpleRunTest
import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.time.TimeProvider

/**
 * Implementation for inhouse Avito report backend
 *
 * todo new instance for every new reportCoordinates
 */
public class AvitoReport(
    private val reportsApi: ReportsApi,
    reportViewerUrl: String,
    loggerFactory: LoggerFactory,
    private val reportCoordinates: ReportCoordinates,
    private val buildId: String,
    private val timeProvider: TimeProvider,
    private val batchSize: Int = 400
) : Report {

    private val logger = loggerFactory.create<AvitoReport>()

    private val reportViewerLinksGenerator = ReportViewerLinksGeneratorImpl(
        reportViewerUrl = reportViewerUrl,
        reportCoordinates = reportCoordinates,
        reportViewerQuery = ReportViewerQuery.createForJvm()
    )

    override val reportLinksGenerator: ReportLinksGenerator
        get() = reportViewerLinksGenerator

    override val testSuiteNameProvider: TestSuiteNameProvider
        get() = reportViewerLinksGenerator

    override fun addTest(testAttempt: TestAttempt) {
        reportsApi.addTest(
            reportCoordinates = reportCoordinates,
            buildId = buildId,
            test = testAttempt.testResult
        ).fold(
            { logger.info("Test ${testAttempt.testResult.name} successfully reported") },
            { logger.warn("Can't report test ${testAttempt.testResult.name}", it) }
        )
    }

    override fun addTest(tests: Collection<AndroidTest>) {
        addTestsBatched("", tests)
    }

    override fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        val testsToSkip = skippedTests
            .map { (test, reason) ->
                AndroidTest.Skipped.fromTestMetadata(
                    testStaticData = test,
                    skipReason = reason,
                    reportTime = timeProvider.nowInSeconds()
                )
            }

        addTestsBatched("skipped", testsToSkip)
    }

    override fun reportLostTests(notReportedTests: Collection<AndroidTest.Lost>) {
        addTestsBatched("lost", notReportedTests)
    }

    override fun getTestResults(): Collection<AndroidTest> {
        TODO("Not yet implemented")
    }

    override fun getPreviousRunsResults(): Result<Map<TestCase, TestStatus>> {
        return reportsApi.getTestsForRunCoordinates(reportCoordinates).mapToRunResults()
    }

    override fun getRunResultsById(id: String): Result<Map<TestCase, TestStatus>> {
        return reportsApi.getTestsForRunId(id).mapToRunResults()
    }

    private fun Result<List<SimpleRunTest>>.mapToRunResults(): Result<Map<TestCase, TestStatus>> =
        map { simpleResults ->
            simpleResults.associate { simpleRunTest ->
                TestCase(simpleRunTest.name, DeviceName(simpleRunTest.deviceName)) to simpleRunTest.status
            }
        }

    private fun addTestsBatched(
        testType: String,
        tests: Collection<AndroidTest>
    ) {
        if (tests.isEmpty()) {
            logger.info("No $testType tests to report")
            return
        }
        tests.actionOnBatches { index, testsBatch ->
            logger.info("Reporting ${testsBatch.size} $testType tests for batch: $index")

            reportsApi.addTests(
                buildId = buildId,
                reportCoordinates = reportCoordinates,
                tests = testsBatch
            ).fold(
                { logger.info("$testType tests successfully reported") },
                { logger.warn("Can't report $testType tests", it) }
            )

            logger.info("Reporting $testType tests for batch: $index completed")
        }
    }

    private fun <T> Collection<T>.actionOnBatches(batchAction: (index: Int, batch: Collection<T>) -> Unit) {
        chunked(batchSize)
            .mapIndexed { index, batch -> index to batch }
            .parallelStream()
            .forEach { (index, batch) -> batchAction(index, batch) }
    }
}
