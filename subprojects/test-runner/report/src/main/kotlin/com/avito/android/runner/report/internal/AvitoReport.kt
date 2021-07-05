package com.avito.android.runner.report.internal

import com.avito.android.Result
import com.avito.android.runner.report.Report
import com.avito.android.runner.report.TestAttempt
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.test.model.TestStatus
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
) : Report {

    private val logger = loggerFactory.create<AvitoReport>()

    /**
     * not sure what, but something bad happens if empty report marked as finished
     */
    private var hasAtLeastOneTestReported = false

    override fun addTest(testAttempt: TestAttempt) {
        hasAtLeastOneTestReported = true

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

    override fun reportLostTests(notReportedTests: Collection<AndroidTest.Lost>) {
        if (notReportedTests.isEmpty()) {
            logger.debug("No lost tests to report")
            return
        }

        notReportedTests.actionOnBatches { index, lostTestsBatch ->
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

        // this is not obvious side-effect, yet very important for correct logic on report-viewer side
        // only finishing report triggers event that leads to parsing suite for TMS
        finish()
    }

    override fun getTestResults(): Collection<AndroidTest> {
        TODO("Not yet implemented")
    }

    override fun getPreviousRunsResults(): Result<Map<TestCase, TestStatus>> {
        return reportsApi.getTestsForRunId(reportCoordinates).map { results ->
            results
                .map { simpleRunTest ->
                    TestCase(simpleRunTest.name, DeviceName(simpleRunTest.deviceName)) to simpleRunTest.status
                }
                .toMap()
        }
    }

    private fun finish() {
        if (hasAtLeastOneTestReported) {
            reportsApi.setFinished(reportCoordinates = reportCoordinates).fold(
                { logger.debug("Test run finished $reportCoordinates") },
                { error -> logger.warn("Can't finish test run $reportCoordinates", error) }
            )
        } else {
            logger.info("Skipping finishing report. It is empty.")
        }
    }

    private fun <T> Collection<T>.actionOnBatches(batchAction: (index: Int, batch: Collection<T>) -> Unit) {
        chunked(batchSize)
            .mapIndexed { index, batch -> index to batch }
            .parallelStream()
            .forEach { (index, batch) -> batchAction(index, batch) }
    }
}
