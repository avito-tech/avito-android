package com.avito.test.summary

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.model.CrossDeviceSuite
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.Team
import com.avito.slack.ConjunctionMessagePredicate
import com.avito.slack.CoroutinesSlackBulkSender
import com.avito.slack.SameAuthorPredicate
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
    reportViewerUrl: String,
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

    private val slackSummaryComposer: SlackSummaryComposer = SlackSummaryComposerImpl(reportViewerUrl)
    private val slackMessageUpdater: SlackMessageUpdater = SlackMessageUpdaterWithThreadMark(
        slackClient = slackClient,
        loggerFactory = loggerFactory,
        threadMessage = "Updated by: $buildUrl"
    )
    private val slackConditionalSender: SlackConditionalSender = SlackConditionalSender(
        slackClient = slackClient,
        updater = slackMessageUpdater,
        condition = ConjunctionMessagePredicate(
            listOf(
                SameAuthorPredicate(slackUserName),
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
        return getReport(reportCoordinates).fold(
            onSuccess = { report ->
                report.id
            },
            onFailure = { throwable ->
                logger.warn("Can't find report for runId=${reportCoordinates.runId}", throwable)
                null
            }
        )
    }
}
