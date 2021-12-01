package com.avito.slack

import com.avito.android.Result
import com.avito.http.HttpClientProvider
import com.avito.http.createStubInstance
import com.avito.kotlin.dsl.getSystemProperty
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SlackConditionalSenderIntegrationTest {

    private val testChannelId = SlackChannel(
        id = getSystemProperty("avito.slack.test.channelId"),
        name = getSystemProperty("avito.slack.test.channel")
    )

    private val testToken = getSystemProperty("avito.slack.test.token")

    private val slackClient: SlackClient = SlackClientImpl(
        serviceName = "slack-integration-tests",
        token = testToken,
        workspace = getSystemProperty("avito.slack.test.workspace"),
        httpClientProvider = HttpClientProvider.createStubInstance()
    )

    @Test
    fun `second message - updates with thread message - if contains same unique string as first one`() {
        val uniqueId = UUID.randomUUID().toString()

        val condition = TextContainsStringCondition(uniqueId)

        val sender = SlackConditionalSender(
            slackClient = slackClient,
            updater = SlackMessageUpdaterWithThreadMark(
                slackClient = slackClient,
                threadMessage = "Updated"
            ),
            condition = condition,
        )

        sender.sendMessage("$uniqueId first message")
        sender.sendMessage("$uniqueId second message")

        val message = slackClient.findMessage(testChannelId, condition)

        assertThat(message).isInstanceOf<Result.Success<*>>()
        assertThat(message.getOrThrow().text).contains("second message")

        // todo assert thread message
    }

    @Test
    fun `second message - updates with thread message - if same author`() {
        val author = UUID.randomUUID().toString()

        val condition = SameAuthorPredicate(author)

        val sender = SlackConditionalSender(
            slackClient = slackClient,
            updater = SlackMessageUpdaterDirectlyToThread(
                slackClient = slackClient,
            ),
            condition = condition,
        )

        val firstMessageTry = sender.sendMessage("first message", author = author)
        val secondMessageTry = sender.sendMessage("second message", author = author)

        assertThat(firstMessageTry).isInstanceOf<Result.Success<*>>()
        val firstMessage = firstMessageTry.getOrThrow()
        assertThat(firstMessage.text).contains("first message")

        assertThat(secondMessageTry).isInstanceOf<Result.Success<*>>()
        assertThat(secondMessageTry.getOrThrow().text).contains("second message")

        assertThat(secondMessageTry.getOrThrow().threadId).isEqualTo(firstMessage.id)
    }

    private fun SlackConditionalSender.sendMessage(
        text: String,
        author: String = "integration test"
    ): Result<SlackMessage> = sendMessage(
        SlackSendMessageRequest(
            channel = testChannelId,
            text = text,
            author = author,
            emoji = ":crazy-robot:"
        )
    ).onFailure { it.printStackTrace() }
}
