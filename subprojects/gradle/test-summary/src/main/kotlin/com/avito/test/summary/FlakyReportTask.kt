package com.avito.test.summary

import com.avito.logger.GradleLoggerFactory
import com.avito.logger.LoggerFactory
import com.avito.report.ReportViewer
import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.slack.ConjunctionMessagePredicate
import com.avito.slack.SameAuthorPredicate
import com.avito.slack.SlackClient
import com.avito.slack.SlackConditionalSender
import com.avito.slack.SlackMessageUpdaterDirectlyToThread
import com.avito.slack.TodayMessageCondition
import com.avito.slack.model.SlackChannel
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class FlakyReportTask : DefaultTask() {

    @get:Input
    abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Input
    abstract val summaryChannel: Property<SlackChannel>

    @get:Input
    abstract val slackUsername: Property<String>

    @get:Input
    abstract val buildUrl: Property<String>

    @get:Input
    abstract val currentBranch: Property<String>

    @get:Internal
    abstract val timeProvider: Property<TimeProvider>

    @get:Internal
    abstract val slackClient: Property<SlackClient>

    @get:Internal
    abstract val reportsApi: Property<ReportsApi>

    @get:Internal
    abstract val reportViewerUrl: Property<String>

    @TaskAction
    fun doWork() {
        val flakyTestInfo = FlakyTestInfo()

        flakyTestInfo.addReport(reportsApi.get().getTestsForRunId(reportCoordinates.get()))

        val loggerFactory: LoggerFactory = GradleLoggerFactory.fromTask(this)
        val timeProvider: TimeProvider = DefaultTimeProvider()

        createFlakyTestReporter(
            summaryChannel = summaryChannel.get(),
            slackUsername = slackUsername.get(),
            reportCoordinates = reportCoordinates.get(),
            reportViewerUrl = reportViewerUrl.get(),
            buildUrl = buildUrl.get(),
            currentBranch = currentBranch.get(),
            loggerFactory = loggerFactory,
            timeProvider = timeProvider
        ).reportSummary(flakyTestInfo.getInfo())
    }

    private fun createFlakyTestReporter(
        summaryChannel: SlackChannel,
        slackUsername: String,
        reportCoordinates: ReportCoordinates,
        reportViewerUrl: String,
        buildUrl: String,
        currentBranch: String,
        loggerFactory: LoggerFactory,
        timeProvider: TimeProvider
    ): FlakyTestReporterImpl {
        val reportViewer = ReportViewer.Impl(reportViewerUrl, reportCoordinates)
        return FlakyTestReporterImpl(
            slackClient = SlackConditionalSender(
                slackClient = slackClient.get(),
                updater = SlackMessageUpdaterDirectlyToThread(slackClient.get(), loggerFactory),
                condition = ConjunctionMessagePredicate(
                    listOf(
                        SameAuthorPredicate(slackUsername),
                        TodayMessageCondition(timeProvider)
                    )
                ),
                loggerFactory = loggerFactory
            ),
            summaryChannel = summaryChannel,
            messageAuthor = slackUsername,
            reportViewer = reportViewer,
            buildUrl = buildUrl,
            currentBranch = currentBranch,
            reportCoordinates = reportCoordinates
        )
    }
}
