package com.avito.test.summary.sender

import com.avito.alertino.AlertinoSenderFactory
import com.avito.alertino.model.AlertinoRecipient
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.Team
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.summary.compose.TestSummaryComposer
import com.avito.test.summary.compose.TestSummaryComposerImpl
import com.avito.test.summary.model.CrossDeviceSuite
import com.avito.test.summary.model.TestSummaryDestination
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import java.io.File

internal class AlertinoTestSummarySender(
    alertinoBaseUrl: String,
    private val alertinoTemplate: String,
    private val alertinoTemplatePlaceholder: String,
    reportViewerUrl: String,
    private val reportsApi: ReportsApi,
    private val buildUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val globalSummaryChannel: AlertinoRecipient,
    private val testSummaryDestination: File,
    private val gson: Gson,
    loggerFactory: LoggerFactory
) : TestSummarySender {

    private val logger = loggerFactory.create<AlertinoTestSummarySender>()
    private val alertinoSender = AlertinoSenderFactory.create(alertinoBaseUrl, loggerFactory)

    private val testSummaryComposer: TestSummaryComposer = TestSummaryComposerImpl(reportViewerUrl)

    override fun send() {
        reportsApi.getTestsForRunCoordinates(reportCoordinates)
            .map { ToCrossDeviceSuiteConverter.convert(it) }
            .fold(
                { suite -> send(suite, requireNotNull(reportsApi.tryGetId(reportCoordinates))) },
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

    private fun send(suite: CrossDeviceSuite, reportId: String) {
        val destinations = gson.fromJson<List<TestSummaryDestination>>(testSummaryDestination.reader())

        destinations.forEach { (team, channel) ->
            val unitSuite = suite.filterTeam(team)

            if (unitSuite.crossDeviceRuns.isNotEmpty()) {
                sendTestSummary(
                    unitSuite,
                    team,
                    reportId,
                    channel
                )
            } else {
                logger.info("Crosse device runs list is empty")
            }
        }

        sendTestSummary(
            suite,
            Team.UNDEFINED,
            reportId,
            globalSummaryChannel,
        )
    }

    private fun sendTestSummary(
        unitSuite: CrossDeviceSuite,
        team: Team,
        reportId: String,
        channel: AlertinoRecipient
    ) {
        testSummaryComposer.composeMessage(
            testData = unitSuite,
            team = team,
            reportCoordinates = reportCoordinates,
            reportId = reportId,
            buildUrl = buildUrl
        ).onSuccess { message ->
            alertinoSender.sendNotification(
                template = alertinoTemplate,
                recipient = channel,
                values = mapOf(alertinoTemplatePlaceholder to message)
            ).onFailure { error ->
                logger.critical("Cannot send message for test summary: ${error.message}", error)
            }
        }.onFailure {
            logger.critical("Cannot compose message for test summary (channel: $channel)", it)
        }
    }
}
