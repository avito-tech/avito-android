package com.avito.test.summary

import com.avito.report.ReportViewer
import com.avito.report.model.CrossDeviceSuite
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
import com.avito.utils.logging.CILogger

interface TestSummarySender {

    fun send(suite: CrossDeviceSuite, reportId: String)
}

/**
 * @param reserveSlackChannel на случай ошибок, чтобы не терять репорты
 */
class TestSummarySenderImplementation(
    slackClient: SlackClient,
    reportViewer: ReportViewer,
    private val logger: CILogger,
    private val buildUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val globalSummaryChannel: SlackChannel = summaryChannel,
    private val unitToChannelMapping: Map<Team, SlackChannel>,
    private val mentionOnFailures: Set<Team> = setOf(Team("buyer-x")),
    private val reserveSlackChannel: SlackChannel = SlackChannel("#speed-testing-team")
) : TestSummarySender {

    private val slackSummaryComposer: SlackSummaryComposer = SlackSummaryComposerImpl(reportViewer)
    private val slackMessageUpdater: SlackMessageUpdater = SlackMessageUpdaterWithThreadMark(
        slackClient = slackClient,
        logger = logger,
        threadMessage = "Updated by: $buildUrl"
    )
    private val slackConditionalSender: SlackConditionalSender = SlackConditionalSender(
        slackClient = slackClient,
        updater = slackMessageUpdater,
        condition = ConjunctionMessageUpdateCondition(
            listOf(
                SameAuthorUpdateCondition(SLACK_USER_NAME),
                TextContainsStringCondition(reportCoordinates.runId)
            )
        ),
        logger = logger
    )
    private val slackBulkSender: SlackBulkSender =
        CoroutinesSlackBulkSender(
            sender = slackConditionalSender, // todo запрашивали опциональность этого (слать в любом случае для некоторых юнитов)
            logger = { s, throwable -> logger.critical(s, throwable) }
        )
    private val slackEmojiProvider = SlackEmojiProvider()

    override fun send(suite: CrossDeviceSuite, reportId: String) {
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
                                author = SLACK_USER_NAME,
                                emoji = slackEmojiProvider.emojiName(unitSuite.percentSuccessOfAutomated)
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
                        author = SLACK_USER_NAME,
                        emoji = slackEmojiProvider.emojiName(suite.percentSuccessOfAutomated)
                    )
                )
            }.onFailure { throwable ->
                logger.critical(
                    "Can't compose slack message for summary: buildUrl=$buildUrl",
                    throwable
                )
            }
        }
    }
}

val summaryChannel = SlackChannel("#android-test-summary")

private const val SLACK_USER_NAME = "Test Analyzer"
