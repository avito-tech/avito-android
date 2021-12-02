package com.avito.test.summary

import com.avito.reportviewer.ReportViewerLinksGeneratorImpl
import com.avito.reportviewer.ReportViewerQuery
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.model.ReportCoordinates
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

public abstract class FlakyReportTask : DefaultTask() {

    @get:Input
    public abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Input
    public abstract val summaryChannel: Property<SlackChannel>

    @get:Input
    public abstract val slackUsername: Property<String>

    @get:Input
    public abstract val buildUrl: Property<String>

    @get:Input
    public abstract val currentBranch: Property<String>

    @get:Internal
    public abstract val timeProvider: Property<TimeProvider>

    @get:Internal
    public abstract val slackClient: Property<SlackClient>

    @get:Internal
    public abstract val reportsApi: Property<ReportsApi>

    @get:Internal
    public abstract val reportViewerUrl: Property<String>

    @TaskAction
    public fun doWork() {
        val flakyTestInfo = FlakyTestInfo()

        flakyTestInfo.addReport(reportsApi.get().getTestsForRunId(reportCoordinates.get()))

        val timeProvider: TimeProvider = DefaultTimeProvider()

        createFlakyTestReporter(
            summaryChannel = summaryChannel.get(),
            slackUsername = slackUsername.get(),
            reportCoordinates = reportCoordinates.get(),
            reportViewerUrl = reportViewerUrl.get(),
            buildUrl = buildUrl.get(),
            currentBranch = currentBranch.get(),
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
        timeProvider: TimeProvider
    ): FlakyTestReporterImpl {
        val reportViewerLinksGenerator = ReportViewerLinksGeneratorImpl(
            reportViewerUrl,
            reportCoordinates,
            ReportViewerQuery.createForJvm()
        )
        return FlakyTestReporterImpl(
            slackClient = SlackConditionalSender(
                slackClient = slackClient.get(),
                updater = SlackMessageUpdaterDirectlyToThread(slackClient.get()),
                condition = ConjunctionMessagePredicate(
                    listOf(
                        SameAuthorPredicate(slackUsername),
                        TodayMessageCondition(timeProvider)
                    )
                ),
            ),
            summaryChannel = summaryChannel,
            messageAuthor = slackUsername,
            reportLinksGenerator = reportViewerLinksGenerator,
            buildUrl = buildUrl,
            currentBranch = currentBranch
        )
    }
}
