package com.avito.slack

import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.utils.logging.CILogger
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SlackConditionalSenderIntegrationTest {

    private val testChannel = SlackChannel(requireNotNull(System.getenv("SLACK_TEST_CHANNEL")))
    private val testToken = requireNotNull(System.getenv("SLACK_TEST_TOKEN"))
    private val slackClient: SlackClient = SlackClient.Impl(testToken, requireNotNull(System.getenv("SLACK_TEST_WORKSPACE")))
    private val logger: CILogger = CILogger.allToStdout

    @Test
    fun `second message - updates with thread message - if contains same unique string as first one`() {
        val uniqueId = UUID.randomUUID().toString()

        val condition = TextContainsStringCondition(uniqueId)

        val sender = SlackConditionalSender(
            slackClient = slackClient,
            updater = SlackMessageUpdaterWithThreadMark(
                slackClient = slackClient,
                logger = logger,
                threadMessage = "Updated"
            ),
            condition = condition,
            logger = logger
        )

        sender.sendMessage("$uniqueId first message")
        sender.sendMessage("$uniqueId second message")

        val message = slackClient.findMessage(testChannel, condition)

        assertThat(message).isInstanceOf(Try.Success::class.java)
        assertThat(message.get().text).contains("second message")

        //todo assert thread message
    }

    @Test
    fun `second message - updates with thread message - if same author`() {
        val author = UUID.randomUUID().toString()

        val condition = SameAuthorUpdateCondition(author)

        val sender = SlackConditionalSender(
            slackClient = slackClient,
            updater = SlackMessageUpdaterDirectlyToThread(
                slackClient = slackClient,
                logger = logger
            ),
            condition = condition,
            logger = logger
        )

        val firstMessageTry = sender.sendMessage("first message", author = author)
        val secondMessageTry = sender.sendMessage("second message", author = author)

        assertThat(firstMessageTry).isInstanceOf(Try.Success::class.java)
        val firstMessage = firstMessageTry.get()
        assertThat(firstMessage.text).contains("first message")

        assertThat(secondMessageTry).isInstanceOf(Try.Success::class.java)
        assertThat(secondMessageTry.get().text).contains("second message")

        // так проверяем что сообщение написано в треде к первому
        assertThat(secondMessageTry.get().threadId).isEqualTo(firstMessage.id)
    }

    private fun SlackConditionalSender.sendMessage(
        text: String,
        author: String = "integration test"
    ): Try<SlackMessage> = sendMessage(
        SlackSendMessageRequest(
            channel = testChannel,
            text = text,
            author = author,
            emoji = ":crazy-robot:"
        )
    ).onFailure { it.printStackTrace() }
}
