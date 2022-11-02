package com.avito.test.summary

import com.avito.logger.GradleLoggerPlugin
import com.avito.report.model.Team
import com.avito.reportviewer.ReportsApiFactory
import com.avito.test.summary.sender.AlertinoTestSummarySender
import com.avito.test.summary.sender.TestSummarySender
import okhttp3.OkHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

public abstract class TestSummaryTask : DefaultTask() {

    @get:Nested
    public abstract val slackExtension: Property<SlackExtension>

    @get:Nested
    public abstract val alertinoExtension: Property<AlertinoExtension>

    @get:Nested
    public abstract val reportViewerExtension: Property<ReportViewerExtension>

    @get:Input
    public abstract val buildUrl: Property<String>

    @TaskAction
    public fun doWork() {
        val loggerFactory = GradleLoggerPlugin.getLoggerFactory(this).get()
        val logger = loggerFactory.create(this::class.java.simpleName)
        logger.info("Executing testSummary task")

        val alertino = alertinoExtension.get()

        val reportViewer = reportViewerExtension.get()

        val reportsApi = ReportsApiFactory.create(
            host = reportViewer.reportsHost.get(),
            builder = OkHttpClient.Builder()
        )

        val testSummarySender: TestSummarySender = AlertinoTestSummarySender(
            alertinoBaseUrl = alertino.alertinoEndpoint.get(),
            alertinoTemplate = alertino.alertinoTemplate.get(),
            alertinoTemplatePlaceholder = alertino.alertinoTemplatePlaceholder.get(),
            reportViewerUrl = reportViewer.url.get(),
            reportsApi = reportsApi,
            buildUrl = buildUrl.get(),
            reportCoordinates = reportViewer.reportCoordinates.get(),
            globalSummaryChannel = alertino.summaryChannel.get(),
            unitToChannelMapping = alertino.unitToChannelMapping.get(),
            mentionOnFailures = alertino.mentionOnFailures.map { set ->
                set.map { Team(it) }.toSet()
            }.get(),
            loggerFactory = loggerFactory
        )

        logger.info("Sending result")
        testSummarySender.send()
    }
}
