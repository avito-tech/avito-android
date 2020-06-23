package com.avito.instrumentation.finalizer

import com.avito.android.stats.StatsDSender
import com.avito.bitbucket.Bitbucket
import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.report.FlakyTestReporterImpl
import com.avito.instrumentation.report.HasFailedTestDeterminer
import com.avito.instrumentation.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.report.JUnitReportWriter
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.report.SendStatisticsActionImpl
import com.avito.report.ReportViewer
import com.avito.slack.ConjunctionMessageUpdateCondition
import com.avito.slack.SameAuthorUpdateCondition
import com.avito.slack.SlackClient
import com.avito.slack.SlackConditionalSender
import com.avito.slack.SlackMessageUpdaterDirectlyToThread
import com.avito.slack.TodayMessageCondition
import com.avito.test.summary.GraphiteRunWriter
import com.avito.test.summary.TestSummarySenderImplementation
import com.avito.test.summary.summaryChannel
import com.avito.time.DefaultTimeProvider
import com.avito.utils.BuildFailer
import com.avito.utils.logging.CILogger
import com.google.common.annotations.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.GsonBuilder

interface FinalizerFactory {

    fun create(): InstrumentationTestActionFinalizer

    class Impl : FinalizerFactory {


        private val params: InstrumentationTestsAction.Params
        private val sourceReport: Report
        private val gson: Gson
        private val slackClient: SlackClient
        private val buildFailer: BuildFailer

        // todo Make generic. Need two realization for InMemory and ReportViewer
        private val reportViewer: ReportViewer
        private val logger: CILogger

        @VisibleForTesting
        internal constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson = GsonBuilder().setPrettyPrinting().create(),
            buildFailer: BuildFailer,
            slackClient: SlackClient
        ) {
            this.params = params
            this.sourceReport = sourceReport
            this.gson = gson
            this.slackClient = slackClient
            this.reportViewer = ReportViewer.Impl(params.reportViewerUrl)
            this.logger = params.logger
            this.buildFailer = buildFailer
        }

        constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson
        ) : this(
            params = params,
            sourceReport = sourceReport,
            gson = gson,
            buildFailer = BuildFailer.RealFailer(),
            slackClient = SlackClient.Impl(params.slackToken, workspace = "avito")
        )

        override fun create(): InstrumentationTestActionFinalizer {

            val hasFailedTestDeterminer: HasFailedTestDeterminer = HasFailedTestDeterminer.Impl(
                suppressFailure = params.suppressFailure,
                suppressFlaky = params.suppressFlaky
            )

            return InstrumentationTestActionFinalizer.Impl(
                hasFailedTestDeterminer = hasFailedTestDeterminer,
                hasNotReportedTestsDeterminer = HasNotReportedTestsDeterminer.Impl(),
                sourceReport = sourceReport,
                params = params,
                flakyTestReporter = createFlakyTestReporter(),
                reportViewer = reportViewer,
                gson = gson,
                jUnitReportWriter = JUnitReportWriter(reportViewer),
                buildFailer = buildFailer,
                sendStatisticsAction = SendStatisticsActionImpl(
                    testSummarySender = TestSummarySenderImplementation(
                        slackClient = slackClient,
                        reportViewer = reportViewer,
                        buildUrl = params.buildUrl,
                        reportCoordinates = params.reportCoordinates,
                        unitToChannelMapping = params.unitToChannelMapping,
                        logger = logger
                    ),
                    report = sourceReport,
                    graphiteRunWriter = GraphiteRunWriter(createStatsDSender()),
                    ciLogger = logger
                ),
                logger = logger
            )
        }

        private fun createStatsDSender(): StatsDSender.Impl {
            return StatsDSender.Impl(
                config = params.statsdConfig,
                logger = { message, error ->
                    if (error != null) logger.info(
                        message,
                        error
                    ) else logger.debug(message)
                }
            )
        }

        private fun createFlakyTestReporter(): FlakyTestReporterImpl {
            return FlakyTestReporterImpl(
                slackClient = SlackConditionalSender(
                    slackClient = slackClient,
                    updater = SlackMessageUpdaterDirectlyToThread(slackClient, logger),
                    condition = ConjunctionMessageUpdateCondition(
                        listOf(
                            SameAuthorUpdateCondition("Flaky Test Detector"),
                            TodayMessageCondition(DefaultTimeProvider())
                        )
                    ),
                    logger = logger
                ),
                summaryChannel = summaryChannel,
                messageAuthor = "Flaky Test Detector",
                bitbucket = Bitbucket.create(
                    bitbucketConfig = params.bitbucketConfig,
                    logger = logger,
                    pullRequestId = params.pullRequestId
                ),
                sourceCommitHash = params.sourceCommitHash,
                reportViewer = reportViewer,
                logger = logger,
                buildUrl = params.buildUrl,
                currentBranch = params.currentBranch,
                reportCoordinates = params.reportCoordinates
            )
        }
    }
}
