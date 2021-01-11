package com.avito.test.summary

import com.avito.logger.GradleLoggerFactory
import com.avito.report.ReportViewer
import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.slack.SlackClient
import com.avito.slack.model.SlackChannel
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class TestSummaryTask : DefaultTask() {

    @get:Input
    abstract val reportCoordinates: Property<ReportCoordinates>

    @get:Input
    abstract val summaryChannel: Property<SlackChannel>

    @get:Input
    abstract val buildUrl: Property<String>

    @Suppress("UnstableApiUsage")
    @get:Input
    abstract val unitToChannelMapping: MapProperty<Team, SlackChannel>

    @get:Input
    abstract val mentionOnFailures: SetProperty<Team>

    @get:Input
    abstract val reserveSlackChannel: Property<SlackChannel>

    @get:Input
    abstract val slackUserName: Property<String>

    @get:Internal
    abstract val slackClient: Property<SlackClient>

    @get:Internal
    abstract val reportsApi: Property<ReportsApi>

    @get:Internal
    abstract val reportViewer: Property<ReportViewer>

    @TaskAction
    fun doWork() {
        val testSummarySender: TestSummarySender = TestSummarySenderImpl(
            slackClient = slackClient.get(),
            reportViewer = reportViewer.get(),
            reportsApi = reportsApi.get(),
            loggerFactory = GradleLoggerFactory.fromTask(this),
            buildUrl = buildUrl.get(),
            reportCoordinates = reportCoordinates.get(),
            globalSummaryChannel = summaryChannel.get(),
            unitToChannelMapping = unitToChannelMapping.get(),
            mentionOnFailures = mentionOnFailures.get(),
            slackUserName = slackUserName.get()
        )

        testSummarySender.send()
    }
}
