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

internal class AlertinoTestSummarySender(
    alertinoBaseUrl: String,
    private val alertinoTemplate: String,
    private val alertinoTemplatePlaceholder: String,
    reportViewerUrl: String,
    private val reportsApi: ReportsApi,
    private val buildUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val globalSummaryChannel: AlertinoRecipient,
    private val unitToChannelMapping: Map<Team, AlertinoRecipient>,
    private val mentionOnFailures: Set<Team>,
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
                {
                    logger.warn("Cannot get tests for RunId[$reportCoordinates]", it)
                }
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
        alertinoSender.run {
            unitToChannelMapping.entries.map { (team, channel) ->
                val unitSuite = suite.filterTeam(team)

                if (unitSuite.crossDeviceRuns.isNotEmpty()) {
                    testSummaryComposer.composeMessage(
                        testData = unitSuite,
                        team = team,
                        mentionOnFailures = mentionOnFailures.contains(team),
                        reportCoordinates = reportCoordinates,
                        reportId = reportId,
                        buildUrl = buildUrl
                    ).onSuccess { message ->
                        sendNotification(
                            template = alertinoTemplate,
                            recipient = channel,
                            values = mapOf(alertinoTemplatePlaceholder to message)
                        )
                    }.onFailure {
                        logger.warn("Cannot compose message for test summary (channel: $channel)", it)
                    }
                } else {
                    logger.info("Crosse device runs list is empty")
                }
            }

            testSummaryComposer.composeMessage(
                testData = suite,
                team = Team.UNDEFINED,
                mentionOnFailures = false,
                reportCoordinates = reportCoordinates,
                reportId = reportId,
                buildUrl = buildUrl
            ).onSuccess {
                sendNotification(
                    template = alertinoTemplate,
                    recipient = globalSummaryChannel,
                    values = mapOf("text" to it)
                ).onFailure { error ->
                    logger.warn("Cannot send message for test summary: ${error.message}")
                }
            }.onFailure {
                logger.warn("Cannot compose message for test summary (channel: $globalSummaryChannel)")
            }
        }
    }
}
