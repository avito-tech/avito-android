package com.avito.test.summary

import com.avito.report.model.Team
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

public abstract class TestSummaryTask : DefaultTask() {

    @get:Input
    public abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Input
    public abstract val summaryChannel: Property<SlackChannel>

    @get:Input
    public abstract val buildUrl: Property<String>

    @get:Input
    public abstract val unitToChannelMapping: MapProperty<Team, SlackChannel>

    @get:Input
    public abstract val mentionOnFailures: SetProperty<Team>

    @get:Input
    public abstract val reserveSlackChannel: Property<SlackChannel>

    @get:Input
    public abstract val slackUsername: Property<String>

    @get:Internal
    public abstract val slackClient: Property<SlackClient>

    @get:Internal
    public abstract val reportsApi: Property<ReportsApi>

    @get:Internal
    public abstract val reportViewerUrl: Property<String>

    @TaskAction
    public fun doWork() {
        val testSummarySender: TestSummarySender = TestSummarySenderImpl(
            slackClient = slackClient.get(),
            reportViewerUrl = reportViewerUrl.get(),
            reportsApi = reportsApi.get(),
            buildUrl = buildUrl.get(),
            reportCoordinates = reportCoordinates.get(),
            globalSummaryChannel = summaryChannel.get(),
            unitToChannelMapping = unitToChannelMapping.get(),
            mentionOnFailures = mentionOnFailures.get(),
            slackUserName = slackUsername.get()
        )

        testSummarySender.send()
    }
}
