package com.avito.test.summary

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.ReportViewer
import com.avito.report.ReportsApi
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.GetReportResult
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.slack.ConjunctionMessageUpdateCondition
import com.avito.slack.CoroutinesSlackBulkSender
import com.avito.slack.SameAuthorUpdateCondition
import com.avito.slack.SlackBulkSender
import com.avito.slack.SlackClient
import com.avito.slack.SlackConditionalSender
import com.avito.slack.SlackMessageUpdater
import com.avito.slack.SlackMessageUpdaterWithThreadMark
import com.avito.slack.TextContainsStringCondition
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.test.summary.compose.SlackSummaryComposer
import com.avito.test.summary.compose.SlackSummaryComposerImpl

internal interface TestSummarySender {

    fun send()
}

internal class TestSummarySenderImpl(
    slackClient: SlackClient,
    reportViewer: ReportViewer,
    private val reportsApi: ReportsApi,
    loggerFactory: LoggerFactory,
    private val buildUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val globalSummaryChannel: SlackChannel,
    private val unitToChannelMapping: Map<Team, SlackChannel>,
    private val mentionOnFailures: Set<Team>,
    private val slackUserName: String
) : TestSummarySender {

    private val logger = loggerFactory.create<TestSummarySender>()

    private val slackSummaryComposer: SlackSummaryComposer = SlackSummaryComposerImpl(reportViewer)
    private val slackMessageUpdater: SlackMessageUpdater = SlackMessageUpdaterWithThreadMark(
        slackClient = slackClient,
        loggerFactory = loggerFactory,
        threadMessage = "Updated by: $buildUrl"
    )
    private val slackConditionalSender: SlackConditionalSender = SlackConditionalSender(
        slackClient = slackClient,
        updater = slackMessageUpdater,
        condition = ConjunctionMessageUpdateCondition(
            listOf(
                SameAuthorUpdateCondition(slackUserName),
                TextContainsStringCondition(reportCoordinates.runId)
            )
        ),
        loggerFactory = loggerFactory
    )
    private val slackBulkSender: SlackBulkSender = CoroutinesSlackBulkSender(
        sender = slackConditionalSender,
        loggerFactory = loggerFactory
    )

    private val slackEmojiProvider = SlackEmojiProvider()

    override fun send() {
        reportsApi.getCrossDeviceTestData(reportCoordinates).fold(
            { suite -> send(suite, requireNotNull(reportsApi.tryGetId(reportCoordinates))) },
            { throwable -> logger.critical("Can't get suite report", throwable) }
        )
    }

    private fun send(suite: CrossDeviceSuite, reportId: String) {
        slackBulkSender.sendBulk {
            unitToChannelMapping.entries.map { (team, channel) ->
                val unitSuite = suite.filterTeam(team)

                if (unitSuite.crossDeviceRuns.isNotEmpty()) {
                    slackSummaryComposer.composeMessage(
                        testData = unitSuite,
                        team = team,
                        mentionOnFailures = mentionOnFailures.contains(team),
                        reportCoordinates = reportCoordinates,
                        reportId = reportId,
                        buildUrl = buildUrl
                    ).onSuccess { message ->
                        sendMessage(
                            SlackSendMessageRequest(
                                channel = channel,
                                text = message,
                                author = slackUserName,
                                emoji = slackEmojiProvider.emojiName(unitSuite.percentSuccessOfAutomated.toInt())
                            )
                        )
                    }.onFailure { throwable ->
                        logger.critical(
                            "Can't compose slack message for unit ${team.name}; buildUrl=$buildUrl",
                            throwable
                        )
                    }
                }
            }

            slackSummaryComposer.composeMessage(
                testData = suite,
                team = Team.UNDEFINED,
                mentionOnFailures = false,
                reportCoordinates = reportCoordinates,
                reportId = reportId,
                buildUrl = buildUrl
            ).onSuccess {
                sendMessage(
                    SlackSendMessageRequest(
                        channel = globalSummaryChannel,
                        text = it,
                        author = slackUserName,
                        emoji = slackEmojiProvider.emojiName(suite.percentSuccessOfAutomated.toInt())
                    )
                )
            }.onFailure { throwable ->
                logger.warn("Can't compose slack message for summary: buildUrl=$buildUrl", throwable)
            }
        }
    }

    private fun ReportsApi.tryGetId(reportCoordinates: ReportCoordinates): String? {
        return when (val result = getReport(reportCoordinates)) {
            is GetReportResult.Found -> result.report.id
            GetReportResult.NotFound -> {
                logger.warn("Can't find report for runId=${reportCoordinates.runId}")
                null
            }
            is GetReportResult.Error -> {
                logger.warn("Can't find report for runId=${reportCoordinates.runId}", result.exception)
                null
            }
        }
    }
}
