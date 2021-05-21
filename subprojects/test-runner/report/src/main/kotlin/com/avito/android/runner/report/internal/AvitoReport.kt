package com.avito.android.runner.report.internal

import com.avito.android.Result
import com.avito.android.runner.report.LegacyReport
import com.avito.android.runner.report.Report
import com.avito.android.runner.report.TestAttempt
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestName
import com.avito.report.model.TestStaticData
import com.avito.time.TimeProvider

/**
 * Implementation for inhouse Avito report backend
 *
 * todo new instance for every new reportCoordinates
 * todo extract batching logic
 * todo move to separate module
 */
internal class AvitoReport(
    private val reportsApi: ReportsApi,
    loggerFactory: LoggerFactory,
    private val reportCoordinates: ReportCoordinates,
    private val buildId: String,
    private val timeProvider: TimeProvider,
    private val batchSize: Int = 400
) : LegacyReport, Report {

    private val logger = loggerFactory.create<AvitoReport>()

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

    override fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        if (skippedTests.isEmpty()) {
            logger.info("No skipped tests to report")
            return
        }

        val testsToSkip = skippedTests
            .map { (test, reason) ->
                AndroidTest.Skipped.fromTestMetadata(
                    testStaticData = test,
                    skipReason = reason,
                    reportTime = timeProvider.nowInSeconds()
                )
            }

        testsToSkip.actionOnBatches { index, testsToSkipBatch ->
            logger.info("Reporting ${testsToSkipBatch.size} skipped tests for batch: $index")

            reportsApi.addTests(
                buildId = buildId,
                reportCoordinates = reportCoordinates,
                tests = testsToSkipBatch
            ).fold(
                { logger.info("Skipped tests successfully reported") },
                { logger.warn("Can't report skipped tests", it) }
            )

            logger.info("Reporting skipped tests for batch: $index completed")
        }
    }

    override fun getTestResults(): Collection<AndroidTest> {
        TODO("Not yet implemented")
    }

    override fun sendLostTests(lostTests: Collection<AndroidTest.Lost>) {
        if (lostTests.isEmpty()) {
            logger.debug("No lost tests to report")
            return
        }

        lostTests.actionOnBatches { index, lostTestsBatch ->
            logger.debug("Reporting ${lostTestsBatch.size} lost tests for batch: $index")

            reportsApi.addTests(
                buildId = buildId,
                reportCoordinates = reportCoordinates,
                tests = lostTestsBatch
            ).fold(
                { logger.debug("Lost tests successfully reported") },
                { logger.warn("Can't report lost tests", it) }
            )

            logger.debug("Reporting lost tests for batch: $index completed")
        }
    }

    override fun finish() {
        val resultsInReport: List<SimpleRunTest> =
            reportsApi.getTestsForRunId(reportCoordinates = reportCoordinates).fold(
                { logger.info("Getting test count in report before closing: ${it.size}"); it },
                { error -> logger.warn("Failed to get tests from report before closing", error); emptyList() }
            )

        if (resultsInReport.isNotEmpty()) {
            reportsApi.setFinished(reportCoordinates = reportCoordinates).fold(
                { logger.debug("Test run finished $reportCoordinates") },
                { error -> logger.warn("Can't finish test run $reportCoordinates", error) }
            )
        } else {
            logger.info("Skipping finishing report. It is empty.")
        }
    }

    override fun getTests(initialSuiteFilter: List<TestName>): Result<List<SimpleRunTest>> {
        return reportsApi.getTestsForRunId(reportCoordinates).map { list ->
            list.filter { TestName(it.className, it.methodName) in initialSuiteFilter }
        }
    }

    private fun <T> Collection<T>.actionOnBatches(batchAction: (index: Int, batch: Collection<T>) -> Unit) {
        chunked(batchSize)
            .mapIndexed { index, batch -> index to batch }
            .parallelStream()
            .forEach { (index, batch) -> batchAction(index, batch) }
    }
}
