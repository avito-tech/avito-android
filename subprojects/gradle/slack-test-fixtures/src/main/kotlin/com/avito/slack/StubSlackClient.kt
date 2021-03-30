package com.avito.slack

import com.avito.android.Result
import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.slack.model.createStubInstance
import java.io.File

class StubSlackClient : SlackClient {

    var requestCount: Int = 0

    var sentMessage: String? = null

    var answeredMessageTimestamp: String? = null

    var answeredMessageText: String? = null

    lateinit var previousMessageTimestamp: String

    var previousMessageFailsWithException: Boolean = false

    var updatedMessageTimestamp: String? = null

    var updatedMessageText: String? = null

    override fun sendMessage(message: SlackSendMessageRequest): Result<SlackMessage> {
        return if (message.threadId == null) {
            sentMessage = message.text
            requestCount++
            Result.Success(SlackMessage.createStubInstance())
        } else {
            answeredMessageText = message.text
            answeredMessageTimestamp = message.threadId
            Result.Success(SlackMessage.createStubInstance())
        }
    }

    override fun findMessage(
        channel: SlackChannel,
        predicate: SlackMessagePredicate
    ): Result<FoundMessage> {
        return if (previousMessageFailsWithException) {
            Result.Failure(Exception("no matter"))
        } else {
            Result.Success(
                previousMessageTimestamp.let { FoundMessage.createStubInstance(timestamp = it) }
            )
        }
    }

    override fun updateMessage(
        channel: SlackChannel,
        text: String,
        messageTimestamp: String
    ): Result<SlackMessage> {
        requestCount++
        updatedMessageText = text
        updatedMessageTimestamp = messageTimestamp
        return Result.Success(SlackMessage.createStubInstance())
    }

    override fun uploadHtml(
        channel: SlackChannel,
        message: String,
        file: File
    ): Result<Unit> {
        TODO("Not yet implemented")
    }
}
