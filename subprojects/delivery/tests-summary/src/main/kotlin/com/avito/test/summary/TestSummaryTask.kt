package com.avito.test.summary

import com.avito.android.stats.statsdConfig
import com.avito.logger.GradleLoggerPlugin
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.summary.json.TestSummaryDestinationDeserializer
import com.avito.test.summary.model.TestSummaryDestination
import com.avito.test.summary.sender.AlertinoTestSummarySender
import com.avito.test.summary.sender.TimeTestSummarySender
import com.avito.test.summary.sender.ToCrossDeviceSuiteConverter
import com.avito.test.summary.sender.testSummarySenders
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

public abstract class TestSummaryTask : DefaultTask() {

    @get:Nested
    public abstract val alertinoExtension: Property<AlertinoExtension>

    @get:Nested
    public abstract val timeExtension: Property<TimeExtension>

    @get:Nested
    public abstract val reportViewerExtension: Property<ReportViewerExtension>

    @get:Input
    public abstract val buildUrl: Property<String>

    @TaskAction
    public fun doWork() {
        val loggerFactory = GradleLoggerPlugin.provideLoggerFactory(this).get()
        val logger = loggerFactory.create("TestSummaryTask")
        logger.info("Executing testSummary task")

        val alertino = alertinoExtension.get()

        val time = timeExtension.get()

        val reportViewer = reportViewerExtension.get()

        val reportCoordinates = reportViewer.reportCoordinates.get()

        val di = TestSummaryDI(
            project.statsdConfig,
            loggerFactory
        )

        val reportsApi = di.provideReportsApi(reportViewer.reportsHost.get())
        val gson = GsonBuilder()
            .registerTypeAdapter(
                TestSummaryDestination::class.java,
                TestSummaryDestinationDeserializer()
            ).create()
        val destinations = gson.fromJson<List<TestSummaryDestination>>(
            alertino.testSummaryDestination.get().asFile.reader()
        )

        val testSummarySenders = testSummarySenders(
            alertinoSender = AlertinoTestSummarySender(
                alertinoBaseUrl = alertino.alertinoEndpoint.get(),
                alertinoTemplate = alertino.alertinoTemplate.get(),
                alertinoTemplatePlaceholder = alertino.alertinoTemplatePlaceholder.get(),
                reportViewerUrl = reportViewer.url.get(),
                buildUrl = buildUrl.get(),
                reportCoordinates = reportCoordinates,
                globalSummaryChannel = alertino.summaryChannel.get(),
                destinations = destinations,
                loggerFactory = loggerFactory
            ),
            timeBaseUrl = time.baseUrl.orNull,
            timeToken = time.token.orNull,
            timeSender = { timeMessenger ->
                TimeTestSummarySender(
                    reportViewerUrl = reportViewer.url.get(),
                    buildUrl = buildUrl.get(),
                    reportCoordinates = reportCoordinates,
                    destinations = destinations,
                    timeMessenger = timeMessenger,
                    loggerFactory = loggerFactory
                )
            },
            logger = logger,
        )

        logger.info("Sending result")
        reportsApi.getTestsForRunCoordinates(reportCoordinates)
            .map { ToCrossDeviceSuiteConverter.convert(it) }
            .fold(
                { suite ->
                    val reportId = requireNotNull(reportsApi.tryGetId(reportCoordinates))
                    testSummarySenders.forEach { it.send(suite, reportId) }
                },
                { logger.critical("Cannot get tests for RunId[$reportCoordinates]", it) }
            )
    }

    private fun ReportsApi.tryGetId(reportCoordinates: ReportCoordinates): String? {
        return getReport(reportCoordinates).fold(
            onSuccess = { report ->
                report.id
            },
            onFailure = {
                // TODO handle throwable
                null
            }
        )
    }
}
