package com.avito.slack

import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.slack.model.createStubInstance
import org.funktionale.tries.Try

class FakeSlackClient : SlackClient {

    var requestCount: Int = 0

    var sentMessage: String? = null

    var answeredMessageTimestamp: String? = null

    var answeredMessageText: String? = null

    lateinit var previousMessageTimestamp: String

    var previousMessageFailsWithException: Boolean = false

    var updatedMessageTimestamp: String? = null

    var updatedMessageText: String? = null

    override fun sendMessage(message: SlackSendMessageRequest): Try<SlackMessage> {
        return if (message.threadId == null) {
            sentMessage = message.text
            requestCount++
            Try.Success(SlackMessage.createStubInstance())
        } else {
            answeredMessageText = message.text
            answeredMessageTimestamp = message.threadId
            Try.Success(SlackMessage.createStubInstance())
        }
    }

    override fun findMessage(channel: SlackChannel, predicate: SlackMessageUpdateCondition): Try<FoundMessage> {
        return if (previousMessageFailsWithException) {
            Try.Failure(Exception("no matter"))
        } else {
            Try.Success(
                previousMessageTimestamp.let { FoundMessage.createStubInstance(timestamp = it) }
            )
        }
    }

    override fun updateMessage(
        channel: SlackChannel,
        text: String,
        messageTimestamp: String
    ): Try<SlackMessage> {
        requestCount++
        updatedMessageText = text
        updatedMessageTimestamp = messageTimestamp
        return Try.Success(SlackMessage.createStubInstance())
    }
}
