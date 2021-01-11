package com.avito.slack

import com.avito.kotlin.dsl.getSystemProperty
import com.avito.logger.StubLoggerFactory
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SlackConditionalSenderIntegrationTest {

    private val testChannel = SlackChannel(getSystemProperty("avito.slack.test.channel"))
    private val testToken = getSystemProperty("avito.slack.test.token")
    private val slackClient: SlackClient = SlackClient.Impl(testToken, getSystemProperty("avito.slack.test.workspace"))
    private val loggerFactory = StubLoggerFactory

    @Test
    fun `second message - updates with thread message - if contains same unique string as first one`() {
        val uniqueId = UUID.randomUUID().toString()

        val condition = TextContainsStringCondition(uniqueId)

        val sender = SlackConditionalSender(
            slackClient = slackClient,
            updater = SlackMessageUpdaterWithThreadMark(
                slackClient = slackClient,
                loggerFactory = loggerFactory,
                threadMessage = "Updated"
            ),
            condition = condition,
            loggerFactory = loggerFactory
        )

        sender.sendMessage("$uniqueId first message")
        sender.sendMessage("$uniqueId second message")

        val message = slackClient.findMessage(testChannel, condition)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get().text).contains("second message")

        // todo assert thread message
    }

    @Test
    fun `second message - updates with thread message - if same author`() {
        val author = UUID.randomUUID().toString()

        val condition = SameAuthorUpdateCondition(author)

        val sender = SlackConditionalSender(
            slackClient = slackClient,
            updater = SlackMessageUpdaterDirectlyToThread(
                slackClient = slackClient,
                loggerFactory = loggerFactory
            ),
            condition = condition,
            loggerFactory = loggerFactory
        )

        val firstMessageTry = sender.sendMessage("first message", author = author)
        val secondMessageTry = sender.sendMessage("second message", author = author)

        assertThat(firstMessageTry).isInstanceOf<Try.Success<*>>()
        val firstMessage = firstMessageTry.get()
        assertThat(firstMessage.text).contains("first message")

        assertThat(secondMessageTry).isInstanceOf<Try.Success<*>>()
        assertThat(secondMessageTry.get().text).contains("second message")

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
