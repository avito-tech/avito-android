package com.avito.test.summary

import com.avito.android.stats.statsdConfig
import com.avito.report.model.Team
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

public abstract class TestSummaryTask : DefaultTask() {

    @get:Nested
    public abstract val slackExtension: Property<SlackExtension>

    @get:Nested
    public abstract val reportViewerExtension: Property<ReportViewerExtension>

    @get:Input
    public abstract val buildUrl: Property<String>

    @TaskAction
    public fun doWork() {
        val testSummaryFactory = TestSummaryFactory(project.statsdConfig)
        val slack = slackExtension.get()
        val notificationClient = testSummaryFactory.createSlackClient(
            token = slack.token.get(),
            workspace = slack.workspace.get()
        )

        val reportViewer = reportViewerExtension.get()
        val reportsApi = testSummaryFactory.createReportsApi(reportViewer.reportsHost.get())

        val testSummarySender: TestSummarySender = TestSummarySenderImpl(
            notificationClient = notificationClient,
            reportViewerUrl = reportViewer.url.get(),
            reportsApi = reportsApi,
            buildUrl = buildUrl.get(),
            reportCoordinates = reportViewer.reportCoordinates.get(),
            globalSummaryChannel = slack.summaryChannel.get(),
            unitToChannelMapping = slack.unitToChannelMapping.get(),
            mentionOnFailures = slack.mentionOnFailures.map { set ->
                set.map { Team(it) }.toSet()
            }.get(),
            slackUserName = slack.username.get()
        )

        testSummarySender.send()
    }
}
