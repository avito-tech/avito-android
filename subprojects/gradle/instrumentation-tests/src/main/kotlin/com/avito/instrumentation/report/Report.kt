package com.avito.instrumentation.report

import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.CreateResult
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.GetReportResult
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.commonLogger
import org.funktionale.tries.Try
import java.io.Serializable

interface Report : ReadReport {

    interface Factory : Serializable {

        sealed class Config : Serializable {

            data class ReportViewerCoordinates(
                val reportCoordinates: ReportCoordinates,
                val buildId: String
            ) : Config()

            data class ReportViewerId(
                val reportId: String
            ) : Config()

            data class InMemory(
                val id: String
            ) : Config()
        }

        fun createReport(config: Config): Report

        fun createReadReport(config: Config): ReadReport

        class StrategyFactory(
            private val factories: Map<in Class<Config>, Factory>
        ) : Factory, Serializable {

            override fun createReport(config: Config): Report =
                getFactory(config).createReport(config)

            override fun createReadReport(config: Config): ReadReport =
                getFactory(config).createReadReport(config)

            private fun getFactory(config: Config): Factory =
                requireNotNull(factories[config::class.java]) {
                    "Factory for config: $config hasn't found. You must register"
                }
        }

        class InMemoryReportFactory : Factory {

            @Transient
            private var reports: MutableMap<Config.InMemory, InMemoryReport> = mutableMapOf()

            // TODO problems with serialization
            @Synchronized
            override fun createReport(config: Config): Report {
                return when (config) {
                    is Config.InMemory -> reports.getOrPut(config, { InMemoryReport(config.id) })
                    is Config.ReportViewerCoordinates -> TODO("Unsupported type")
                    is Config.ReportViewerId -> TODO("Unsupported type")
                }
            }

            @Synchronized
            override fun createReadReport(config: Config): ReadReport {
                return when (config) {
                    is Config.InMemory -> reports.getOrPut(config, { InMemoryReport(config.id) })
                    is Config.ReportViewerCoordinates -> TODO("Unsupported type")
                    is Config.ReportViewerId -> TODO("Unsupported type")
                }
            }
        }

        class ReportViewerFactory(
            val reportApiUrl: String,
            val reportApiFallbackUrl: String,
            val ciLogger: CILogger,
            val verboseHttp: Boolean
        ) : Factory {

            @Transient
            private lateinit var reportsApi: ReportsApi

            override fun createReport(config: Config): Report {
                return when (config) {
                    is Config.ReportViewerCoordinates -> {
                        ensureInitializedReportsApi()
                        Impl(
                            reportsApi = reportsApi,
                            logger = ciLogger,
                            reportCoordinates = config.reportCoordinates,
                            buildId = config.buildId
                        )
                    }
                    else -> throwUnsupportedConfigException(config)
                }
            }

            override fun createReadReport(config: Config): ReadReport {
                return when (config) {
                    is Config.ReportViewerCoordinates -> {
                        ensureInitializedReportsApi()
                        ReadReport.ReportCoordinates(
                            reportsFetchApi = reportsApi,
                            coordinates = config.reportCoordinates
                        )
                    }
                    is Config.ReportViewerId -> {
                        ensureInitializedReportsApi()
                        ReadReport.Id(
                            reportsFetchApi = reportsApi,
                            id = config.reportId
                        )
                    }
                    is Config.InMemory -> TODO("Unsupported type")
                }
            }

            private fun throwUnsupportedConfigException(config: Config): Nothing {
                throw IllegalArgumentException("Unsupported config: $config")
            }

            private fun ensureInitializedReportsApi() {
                if (!::reportsApi.isInitialized) {
                    reportsApi = ReportsApi.create(
                        host = reportApiUrl,
                        fallbackUrl = reportApiFallbackUrl,
                        logger = commonLogger(ciLogger),
                        verboseHttp = verboseHttp
                    )
                }
            }
        }
    }

    fun tryCreate(apiUrl: String, gitBranch: String, gitCommit: String)

    fun tryGetId(): String?

    fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, String>>)

    fun sendLostTests(lostTests: List<AndroidTest.Lost>)

    fun sendCompletedTest(completedTest: AndroidTest.Completed)

    fun finish()

    fun markAsSuccessful(testRunId: String, author: String, comment: String): Try<Unit>

    fun getCrossDeviceTestData(): Try<CrossDeviceSuite>

    // todo новый инстанс на каждый reportCoordinates, сейчас уже неверно шарится между rerun report и основным
    // todo перенести логику с батчами в reportsApi
    class Impl(
        private val reportsApi: ReportsApi,
        private val logger: CILogger,
        private val reportCoordinates: ReportCoordinates,
        private val buildId: String,
        private val timeProvider: TimeProvider = DefaultTimeProvider(),
        private val batchSize: Int = 400
    ) : Report {

        override fun tryCreate(apiUrl: String, gitBranch: String, gitCommit: String) {
            return when (val result = reportsApi.create(reportCoordinates, buildId, apiUrl, gitBranch, gitCommit)) {
                is CreateResult.Created ->
                    logger.info("Report created, id=${result.id}")
                CreateResult.AlreadyCreated ->
                    logger.info(
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
                    logger.critical("Can't find report for runId=${reportCoordinates.runId}")
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
}
