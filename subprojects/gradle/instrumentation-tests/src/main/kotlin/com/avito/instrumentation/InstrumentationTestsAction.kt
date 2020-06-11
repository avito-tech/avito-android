package com.avito.instrumentation

import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.bitbucket.Bitbucket
import com.avito.bitbucket.BitbucketConfig
import com.avito.buildontarget.BuildOnTargetCommitForTest
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.executing.TestExecutorFactory
import com.avito.instrumentation.finalizer.InstrumentationActionFinalizer
import com.avito.instrumentation.report.HasFailedTestDeterminer
import com.avito.instrumentation.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.report.JUnitReportWriter
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.report.SendStatisticsAction
import com.avito.instrumentation.report.listener.ReportViewerTestReporter
import com.avito.instrumentation.scheduling.InstrumentationTestsScheduler
import com.avito.instrumentation.scheduling.PerformanceTestsScheduler
import com.avito.instrumentation.scheduling.TestsRunner
import com.avito.instrumentation.scheduling.TestsRunnerImplementation
import com.avito.instrumentation.scheduling.TestsScheduler
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.instrumentation.suite.dex.TestSuiteLoader
import com.avito.instrumentation.suite.dex.TestSuiteLoaderImpl
import com.avito.instrumentation.suite.filter.FilterFactory
import com.avito.instrumentation.suite.filter.FilterInfoWriter
import com.avito.report.ReportViewer
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import com.avito.test.summary.GraphiteRunWriter
import com.avito.test.summary.TestSummarySenderImplementation
import com.avito.utils.BuildFailer
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.hasFileContent
import com.avito.utils.logging.CILogger
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.Serializable
import javax.inject.Inject

class InstrumentationTestsAction(
    private val params: Params,
    private val logger: CILogger,
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create(),
    private val sourceReport: Report = params.reportFactory.createReport(params.reportConfig),
    private val targetReport: Report = params.reportFactory.createReport(params.targetReportConfig),
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
        logger = logger,
        registry = params.registry
    ),
    private val testSuiteLoader: TestSuiteLoader = TestSuiteLoaderImpl(),

    private val filterFactory: FilterFactory = FilterFactory.create(
        filterData = params.instrumentationConfiguration.filter,
        impactAnalysisResult = params.impactAnalysisResult,
        factory = params.reportFactory,
        reportConfig = params.reportConfig
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
        reportCoordinates = params.reportCoordinates,
        sourceReport = sourceReport,
        targetReport = targetReport,
        targetReportCoordinates = params.targetReportCoordinates,
        testSuiteProvider = testSuiteProvider,
        testSuiteLoader = testSuiteLoader
    ),
    private val instrumentationTestsScheduler: TestsScheduler = InstrumentationTestsScheduler(
        logger = logger,
        testsRunner = testRunner,
        params = params,
        reportCoordinates = params.reportCoordinates,
        targetReportCoordinates = params.targetReportCoordinates,
        testSuiteProvider = testSuiteProvider,
        sourceReport = sourceReport,
        targetReport = targetReport,
        testSuiteLoader = testSuiteLoader,
        gson = gson
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
    buildFailer: BuildFailer = BuildFailer.RealFailer(),
    // todo Make generic. Need two realization for InMemory and ReportViewer
    private val reportViewer: ReportViewer = ReportViewer.Impl(params.reportViewerUrl),
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
    ),
    private val filterInfoWriter: FilterInfoWriter = FilterInfoWriter.Impl(
        outputDir = params.outputDir,
        gson = gson
    )
) : Runnable,
    FlakyTestReporterFactory by FlakyTestReporterFactory.Impl(
        slackClient = slackClient,
        logger = logger,
        bitbucket = bitbucket,
        reportViewer = reportViewer,
        sourceCommitHash = params.sourceCommitHash
    ) {

    /**
     * for Worker API
     */
    @Suppress("unused")
    @Inject
    constructor(params: Params) : this(params, params.logger)

    private val actionFinalizer: InstrumentationActionFinalizer = InstrumentationActionFinalizer.Impl(
        logger = logger,
        jUnitReportWriter = JUnitReportWriter(reportViewer),
        gson = gson,
        buildFailer = buildFailer
    )

    private fun fromParams(params: Params): BuildOnTargetCommitForTest.Result {
        return if (!params.apkOnTargetCommit.hasFileContent() || !params.testApkOnTargetCommit.hasFileContent()) {
            BuildOnTargetCommitForTest.Result.ApksUnavailable
        } else {
            BuildOnTargetCommitForTest.Result.OK(
                mainApk = params.apkOnTargetCommit,
                testApk = params.testApkOnTargetCommit
            )
        }
    }

    override fun run() {
        logger.debug("Starting instrumentation tests action for configuration: ${params.instrumentationConfiguration}")
        filterInfoWriter.writeFilterConfig(params.instrumentationConfiguration.filter)

        val buildOnTargetCommitResult = fromParams(params)
        val testsExecutionResults: TestsScheduler.Result =
            if (params.instrumentationConfiguration.performanceType != null) {
                performanceTestsScheduler.schedule(
                    buildOnTargetCommitResult = buildOnTargetCommitResult
                )
            } else {
                instrumentationTestsScheduler.schedule(
                    buildOnTargetCommitResult = buildOnTargetCommitResult
                )
            }

        filterInfoWriter.writeAppliedFilter(testsExecutionResults.initialTestSuite.appliedFilter)
        filterInfoWriter.writeFilterExcludes(testsExecutionResults.initialTestSuite.skippedTests)

        val testRunResult = TestRunResult(
            reportedTests = testsExecutionResults.initialTestsResult.getOrElse { emptyList() },
            failed = hasFailedTestDeterminer.determine(
                runResult = testsExecutionResults.testResultsAfterBranchReruns
            ),
            notReported = hasNotReportedTestsDeterminer.determine(
                runResult = testsExecutionResults.initialTestsResult,
                allTests = testsExecutionResults.initialTestSuite.testsToRun.map { it.test }
            )
        )

        if (testRunResult.notReported is HasNotReportedTestsDeterminer.Result.HasNotReportedTests) {
            sourceReport.sendLostTests(lostTests = testRunResult.notReported.lostTests)
        }

        sourceReport.finish(
            isFullTestSuite = params.isFullTestSuite
        )

        if (params.instrumentationConfiguration.reportFlakyTests) {
            flakyTestReporter.reportSummary(
                info = testsExecutionResults.flakyInfo,
                buildUrl = params.buildUrl,
                currentBranch = params.currentBranch,
                reportCoordinates = params.reportCoordinates,
                targetReportCoordinates = params.targetReportCoordinates
            ).onFailure { logger.critical("Can't send flaky test report", it) }
        }

        val reportId = requireNotNull(sourceReport.tryGetId())
        sendStatistics(reportId)
        val reportViewerUrl = reportViewer.generateReportUrl(
            params.reportCoordinates,
            onlyFailures = testRunResult.failed !is HasFailedTestDeterminer.Result.NoFailed
        )

        actionFinalizer.finalize(
            params.outputDir,
            params.reportCoordinates,
            reportViewerUrl,
            testRunResult
        )
    }

    private fun sendStatistics(reportId: String) {
        if (params.sendStatistics) {
            SendStatisticsAction(
                reportId = reportId,
                testSummarySender = TestSummarySenderImplementation(
                    slackClient = slackClient,
                    reportViewer = reportViewer,
                    buildUrl = params.buildUrl,
                    reportCoordinates = params.reportCoordinates,
                    unitToChannelMapping = params.unitToChannelMapping,
                    logger = logger
                ),
                report = sourceReport,
                graphiteRunWriter = GraphiteRunWriter(statsSender),
                ciLogger = logger
            ).send()
        } else {
            logger.info("Send statistics disabled")
        }
    }

    companion object {
        const val RUN_ON_TARGET_BRANCH_SLUG = "rerun"
    }

    data class Params(
        val mainApk: File?,
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
        val kubernetesCredentials: KubernetesCredentials,
        val projectName: String,
        val suppressFailure: Boolean,
        val suppressFlaky: Boolean,
        val impactAnalysisResult: File?,
        val logger: CILogger,
        val outputDir: File,
        val sendStatistics: Boolean,
        val isFullTestSuite: Boolean,
        val slackToken: String,
        val reportViewerUrl: String,
        val fileStorageUrl: String,
        val bitbucketConfig: BitbucketConfig,
        val statsdConfig: StatsDConfig,
        val unitToChannelMapping: Map<Team, SlackChannel>,
        val registry: String,
        val reportFactory: Report.Factory,
        val reportConfig: Report.Factory.Config,
        val targetReportConfig: Report.Factory.Config,
        @Deprecated("Will be removed")
        val reportCoordinates: ReportCoordinates,
        @Deprecated("Will be removed")
        val targetReportCoordinates: ReportCoordinates
    ) : Serializable {
        companion object
    }
}
