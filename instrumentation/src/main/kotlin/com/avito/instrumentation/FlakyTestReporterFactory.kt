package com.avito.instrumentation

import com.avito.bitbucket.Bitbucket
import com.avito.instrumentation.report.FlakyTestReporter
import com.avito.report.ReportViewer
import com.avito.slack.ConjunctionMessageUpdateCondition
import com.avito.slack.SameAuthorUpdateCondition
import com.avito.slack.SlackClient
import com.avito.slack.SlackConditionalSender
import com.avito.slack.SlackMessageUpdaterDirectlyToThread
import com.avito.slack.TodayMessageCondition
import com.avito.test.summary.summaryChannel
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.logging.CILogger

interface FlakyTestReporterFactory {

    val flakyTestReporter: FlakyTestReporter

    class Impl(
        slackClient: SlackClient,
        logger: CILogger,
        bitbucket: Bitbucket,
        reportViewer: ReportViewer,
        sourceCommitHash: String
    ) : FlakyTestReporterFactory {

        private val timeProvider: TimeProvider = DefaultTimeProvider()

        private val flakyTestMessageAuthor: String = "Flaky Test Detector"

        override val flakyTestReporter: FlakyTestReporter =
            FlakyTestReporter(
                slackClient = SlackConditionalSender(
                    slackClient = slackClient,
                    updater = SlackMessageUpdaterDirectlyToThread(slackClient, logger),
                    condition = ConjunctionMessageUpdateCondition(
                        listOf(
                            SameAuthorUpdateCondition(flakyTestMessageAuthor),
                            TodayMessageCondition(timeProvider)
                        )
                    ),
                    logger = logger
                ),
                summaryChannel = summaryChannel,
                messageAuthor = flakyTestMessageAuthor,
                bitbucket = bitbucket,
                sourceCommitHash = sourceCommitHash,
                reportViewer = reportViewer,
                logger = logger
            )
    }
}
