package com.avito.instrumentation

import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.bitbucket.Bitbucket
import com.avito.bitbucket.BitbucketConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.executing.TestExecutorFactory
import com.avito.instrumentation.report.HasFailedTestDeterminer
import com.avito.instrumentation.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.report.JUnitReportWriter
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.report.SendStatisticsAction
import com.avito.instrumentation.report.listener.ReportViewerTestReporter
import com.avito.instrumentation.rerun.BuildOnTargetCommitForTest
import com.avito.instrumentation.scheduling.InstrumentationTestsScheduler
import com.avito.instrumentation.scheduling.PerformanceTestsScheduler
import com.avito.instrumentation.scheduling.TestsRunner
import com.avito.instrumentation.scheduling.TestsRunnerImplementation
import com.avito.instrumentation.scheduling.TestsScheduler
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.instrumentation.suite.dex.TestSuiteLoader
import com.avito.instrumentation.suite.dex.TestSuiteLoaderImpl
import com.avito.instrumentation.suite.dex.check.AllChecks
import com.avito.instrumentation.suite.filter.FilterFactory
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.logger.Logger
import com.avito.report.ReportViewer
import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.test.summary.GraphiteRunWriter
import com.avito.test.summary.TestSummarySenderImplementation
import com.avito.utils.BuildFailer
import com.avito.utils.createOrClear
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.logging.CILogger
import okhttp3.HttpUrl
import java.io.File
import java.io.Serializable
import javax.inject.Inject

/**
 * @param params параметры собраны в serializable box, чтобы
 * получить типизацию при создании инстанса рефлекшном для Worker Api
 */
class InstrumentationTestsAction(
    private val params: Params,
    private val logger: CILogger,
    private val reportsApi: ReportsApi = ReportsApi.create(
        host = params.reportApiUrl,
        fallbackUrl = params.reportApiFallbackUrl,
        logger = object : Logger {
            override fun debug(msg: String) {
                logger.debug(msg)
            }

            override fun exception(msg: String, error: Throwable) {
                logger.debug(msg, error)
            }

            override fun critical(msg: String, error: Throwable) {
                logger.debug(msg, error)
            }
        }

    ),
    private val reportCoordinates: ReportCoordinates = params
        .instrumentationConfiguration
        .instrumentationParams
        .reportCoordinates(),

    private val targetReportCoordinates: ReportCoordinates = reportCoordinates.copy(
        jobSlug = "${reportCoordinates.jobSlug}-$RUN_ON_TARGET_BRANCH_SLUG"
    ),
    private val sourceReport: Report = Report.Impl(
        reportsApi = reportsApi,
        logger = logger,
        reportCoordinates = reportCoordinates,
        buildId = params.buildId
    ),
    private val targetReport: Report = Report.Impl(
        reportsApi = reportsApi,
        logger = logger,
        reportCoordinates = targetReportCoordinates,
        buildId = params.buildId
    ),
    private val testExecutorFactory: TestExecutorFactory = TestExecutorFactory.Implementation(),
    private val testRunner: TestsRunner = TestsRunnerImplementation(
        testExecutorFactory = testExecutorFactory,
        kubernetesCredentials = params.kubernetesCredentials,
        testReporterFactory = { testSuite, outputDir, report ->
            ReportViewerTestReporter(
                logger = logger,
                testSuite = testSuite,
                report = report,
                fileStorageUrl = params.fileStorageUrl,
                logcatDir = outputDir
            )
        },
        buildId = params.buildId,
        buildType = params.buildType,
        projectName = params.projectName,
        executionParameters = params.executionParameters,
        outputDirectory = params.outputDir,
        instrumentationConfiguration = params.instrumentationConfiguration,
        reportsApi = reportsApi,
        logger = logger,
        registry = params.registry
    ),
    private val testSuiteLoader: TestSuiteLoader = TestSuiteLoaderImpl(),

    private val filterFactory: FilterFactory = FilterFactory.create(
        filterData = params.instrumentationConfiguration.filter,
        impactAnalysisResult = params.impactAnalysisResult,
        reportCoordinates = reportCoordinates,
        reportsFetchApi = reportsApi
    ),
    private val testSuiteProvider: TestSuiteProvider = TestSuiteProvider.Impl(
        report = sourceReport,
        targets = params.instrumentationConfiguration.targets,
        filterFactory = filterFactory,
        reportSkippedTests = params.instrumentationConfiguration.reportSkippedTests
    ),
    private val performanceTestsScheduler: TestsScheduler = PerformanceTestsScheduler(
        testsRunner = testRunner,
        params = params,
        reportCoordinates = reportCoordinates,
        sourceReport = sourceReport,
        targetReport = targetReport,
        targetReportCoordinates = targetReportCoordinates,
        testSuiteProvider = testSuiteProvider
    ),
    private val instrumentationTestsScheduler: TestsScheduler = InstrumentationTestsScheduler(
        logger = logger,
        testsRunner = testRunner,
        reportsApi = reportsApi,
        params = params,
        reportCoordinates = reportCoordinates,
        targetReportCoordinates = targetReportCoordinates,
        testSuiteProvider = testSuiteProvider,
        sourceReport = sourceReport,
        targetReport = targetReport
    ),
    private val statsSender: StatsDSender = StatsDSender.Impl(
        config = params.statsdConfig,
        logger = { message, error ->
            if (error != null) logger.info(
                message,
                error
            ) else logger.debug(message)
        }
    ),
    private val reportViewer: ReportViewer = ReportViewer.Impl(params.reportViewerUrl),
    private val buildFailer: BuildFailer = BuildFailer.RealFailer(),
    private val jUnitReportWriter: JUnitReportWriter = JUnitReportWriter(reportViewer),
    private val hasNotReportedTestsDeterminer: HasNotReportedTestsDeterminer = HasNotReportedTestsDeterminer.Impl(),
    private val hasFailedTestDeterminer: HasFailedTestDeterminer = HasFailedTestDeterminer.Impl(
        suppressFailure = params.suppressFailure,
        suppressFlaky = params.suppressFlaky
    ),
    private val slackClient: SlackClient = SlackClient.Impl(params.slackToken, workspace = "avito"),
    private val bitbucket: Bitbucket = Bitbucket.create(
        bitbucketConfig = params.bitbucketConfig,
        logger = logger,
        pullRequestId = params.pullRequestId
    )
) : Runnable,
    FlakyTestReporterFactory by FlakyTestReporterFactory.Impl(
        slackClient = slackClient,
        logger = logger,
        bitbucket = bitbucket,
        reportViewer = reportViewer,
        sourceCommitHash = params.sourceCommitHash
    ) {

    @Inject
    constructor(params: Params) : this(params, params.logger)

    override fun run() {
        logger.info("Starting instrumentation tests action for configuration: ${params.instrumentationConfiguration}")

        val initialTestSuite: List<TestWithTarget> =
            testSuiteProvider.getInitialTestSuite(
                tests = testSuiteLoader.loadTestSuite(params.testApk, AllChecks())
            )

        val buildOnTargetCommitResult = BuildOnTargetCommitForTest.fromParams(params)

        val testsExecutionResults: TestsScheduler.Result =
            if (params.instrumentationConfiguration.performanceType != null) {
                performanceTestsScheduler.schedule(
                    initialTestsSuite = initialTestSuite,
                    buildOnTargetCommitResult = buildOnTargetCommitResult
                )
            } else {
                instrumentationTestsScheduler.schedule(
                    initialTestsSuite = initialTestSuite,
                    buildOnTargetCommitResult = buildOnTargetCommitResult
                )
            }

        val testRunResult = TestRunResult(
            reportedTests = testsExecutionResults.initialTestsResult.getOrElse { emptyList() },
            failed = hasFailedTestDeterminer.determine(
                runResult = testsExecutionResults.testResultsAfterBranchReruns
            ),
            notReported = hasNotReportedTestsDeterminer.determine(
                runResult = testsExecutionResults.initialTestsResult,
                allTests = initialTestSuite.map { it.test }
            )
        )

        if (testRunResult.notReported is HasNotReportedTestsDeterminer.Result.HasNotReportedTests) {
            sourceReport.sendLostTests(lostTests = testRunResult.notReported.lostTests)
        }

        jUnitReportWriter.write(
            reportCoordinates = reportCoordinates,
            testRunResult = testRunResult,
            destination = junitFile(params.outputDir)
        )

        val reportViewerUrl = reportViewer.generateReportUrl(
            reportCoordinates,
            onlyFailures = testRunResult.failed !is HasFailedTestDeterminer.Result.NoFailed
        )

        writeReportViewerLinkFile(
            reportViewerUrl = reportViewerUrl,
            reportFile = reportViewerFile(params.outputDir)
        )

        sourceReport.finish(
            isFullTestSuite = params.isFullTestSuite,
            reportViewerUrl = reportViewerUrl
        )

        if (params.instrumentationConfiguration.reportFlakyTests) {
            flakyTestReporter.reportSummary(
                info = testsExecutionResults.flakyInfo,
                buildUrl = params.buildUrl,
                currentBranch = params.currentBranch,
                reportCoordinates = reportCoordinates,
                rerunReportCoordinates = targetReportCoordinates
            ).onFailure { logger.critical("Can't send flaky test report", it) }
        }

        if (params.reportId != null) {
            sendStatistics(params.reportId)
        } else {
            logger.critical("Cannot find report id for $reportCoordinates. Skip statistic sending")
        }
        val verdict = testRunResult.verdict
        when (verdict) {
            is TestRunResult.Verdict.Success -> logger.info(verdict.message)
            is TestRunResult.Verdict.Failed -> buildFailer.failBuild(
                verdict.message,
                verdict.throwable
            )
        }
    }

    private fun sendStatistics(reportId: String) {
        if (params.sendStatistics) {
            SendStatisticsAction(
                reportId = reportId,
                reportCoordinates = reportCoordinates,
                testSummarySender = TestSummarySenderImplementation(
                    slackClient = slackClient,
                    reportViewer = reportViewer,
                    buildUrl = params.buildUrl,
                    reportCoordinates = reportCoordinates,
                    unitToChannelMapping = params.unitToChannelMapping,
                    logger = logger
                ),
                reportsApi = reportsApi,
                graphiteRunWriter = GraphiteRunWriter(statsSender),
                ciLogger = logger
            ).send()
        } else {
            logger.info("Send statistics disabled")
        }
    }

    /**
     * teamcity XML report processing
     */
    private fun junitFile(outputDir: File): File = File(outputDir, "junit-report.xml")

    /**
     * teamcity report tab
     */
    private fun reportViewerFile(outputDir: File): File = File(outputDir, "rv.html")

    /**
     * todo тест что файл с репортом есть даже если фейл билда
     * todo если репорт пустой, мы можем корректную ошибку печатать (возможно это лучше сделать на стороне RM)
     */
    private fun writeReportViewerLinkFile(
        reportViewerUrl: HttpUrl,
        reportFile: File
    ) {
        reportFile.createOrClear()
        reportFile.writeText("<script>location=\"${reportViewerUrl}\"</script>")
    }

    companion object {

        //todo прокидывать в performance таску параметром
        const val RUN_ON_TARGET_BRANCH_SLUG = "rerun"
    }

    // TODO: вынести все параметры для зависимостей в конфиги по аналогии с BitbucketConfig, StatsDConfig
    data class Params(
        val mainApk: File,
        val testApk: File,
        val apkOnTargetCommit: File?,
        val testApkOnTargetCommit: File?,
        val instrumentationConfiguration: InstrumentationConfiguration.Data,
        val executionParameters: ExecutionParameters,
        val buildId: String,
        val buildType: String,
        val pullRequestId: Int?,
        val buildUrl: String,
        val currentBranch: String,
        val sourceCommitHash: String,
        val targetCommit: String?,
        val kubernetesCredentials: KubernetesCredentials,
        val projectName: String,
        val testedVariantName: String,
        val suppressFailure: Boolean,
        val suppressFlaky: Boolean,
        val impactAnalysisResult: File?,
        val logger: CILogger,
        val outputDir: File, //todo валидация на соответствие teamcity конфигу
        val sendStatistics: Boolean,
        val isFullTestSuite: Boolean,
        val slackToken: String,
        val reportId: String?,
        val reportApiUrl: String,
        val reportApiFallbackUrl: String,
        val reportViewerUrl: String,
        val fileStorageUrl: String,
        val bitbucketConfig: BitbucketConfig,
        val statsdConfig: StatsDConfig,
        val unitToChannelMapping: Map<Team, SlackChannel>,
        val registry: String
    ) : Serializable {
        companion object
    }
}
