package com.avito.android.runner.report

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.CreateResult
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.GetReportResult
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.time.TimeProvider
import org.funktionale.tries.Try

/**
 * Implementation for inhouse Avito report backend
 *
 * todo new instance for every new reportCoordinates
 * todo extract batching logic
 */
internal class AvitoReport(
    private val reportsApi: ReportsApi,
    loggerFactory: LoggerFactory,
    private val reportCoordinates: ReportCoordinates,
    private val buildId: String,
    private val timeProvider: TimeProvider,
    private val batchSize: Int = 400
) : Report {

    private val logger = loggerFactory.create<Report>()

    override fun tryCreate(testHost: String, gitBranch: String, gitCommit: String) {
        return when (val result = reportsApi.create(reportCoordinates, buildId, testHost, gitBranch, gitCommit)) {
            is CreateResult.Created ->
                logger.debug("Report created, id=${result.id}")
            CreateResult.AlreadyCreated ->
                logger.debug(
                    "Can't tryCreate report, already created, " +
                        "it's ok if we call it N(=release configurations) times"
                )
            is CreateResult.Failed ->
                logger.critical("Can't tryCreate report", result.exception)
        }
    }

    // todo закешировать после разделения инстансов
    override fun tryGetId(): String? {
        return when (val result = reportsApi.getReport(reportCoordinates)) {
            is GetReportResult.Found -> result.report.id
            GetReportResult.NotFound -> {
                logger.critical("Can't find report for runId=${reportCoordinates.runId}", NoSuchElementException())
                null
            }
            is GetReportResult.Error -> {
                logger.critical("Can't find report for runId=${reportCoordinates.runId}", result.exception)
                null
            }
        }
    }

    override fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        if (skippedTests.isEmpty()) {
            logger.info("No skipped tests to report")
            return
        }

        val testsToSkip = skippedTests
            .map { (test, reason) ->
                AndroidTest.Skipped.fromTestMetadata(
                    testStaticData = test,
                    skipReason = reason,
                    reportTime = timeProvider.nowInMillis()
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
                { logger.critical("Can't report skipped tests", it) }
            )

            logger.info("Reporting skipped tests for batch: $index completed")
        }
    }

    override fun sendLostTests(lostTests: List<AndroidTest.Lost>) {
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

    override fun sendCompletedTest(completedTest: AndroidTest.Completed) {
        reportsApi.addTests(reportCoordinates, buildId, tests = listOf(completedTest)).fold(
            { logger.info("Test ${completedTest.name} successfully reported") },
            { logger.critical("Can't report test ${completedTest.name}", it) }
        )
    }

    override fun finish() {
        val resultsInReport: List<SimpleRunTest> =
            reportsApi.getTestsForRunId(reportCoordinates = reportCoordinates).fold(
                { logger.info("Getting test count in report before closing: ${it.size}"); it },
                { error -> logger.critical("Failed to get tests from report before closing", error); emptyList() }
            )

        if (resultsInReport.isNotEmpty()) {
            reportsApi.setFinished(reportCoordinates = reportCoordinates).fold(
                { logger.debug("Test run finished $reportCoordinates") },
                { error -> logger.critical("Can't finish test run $reportCoordinates", error) }
            )
        } else {
            logger.info("Skipping finishing report. It is empty.")
        }
    }

    override fun getTests(): Try<List<SimpleRunTest>> {
        return reportsApi.getTestsForRunId(reportCoordinates)
    }

    override fun markAsSuccessful(testRunId: String, author: String, comment: String): Try<Unit> {
        return reportsApi.markAsSuccessful(testRunId, author, comment)
    }

    override fun getCrossDeviceTestData(): Try<CrossDeviceSuite> {
        return reportsApi.getCrossDeviceTestData(reportCoordinates)
    }

    private fun <T> Collection<T>.actionOnBatches(batchAction: (index: Int, batch: Collection<T>) -> Unit) {
        chunked(batchSize)
            .mapIndexed { index, batch -> index to batch }
            .parallelStream()
            .forEach { (index, batch) -> batchAction(index, batch) }
    }
}
