package com.avito.slack

import com.avito.android.Result
import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.slack.model.createStubInstance
import java.io.File

public class StubSlackClient : SlackClient {

    public var requestCount: Int = 0

    public var sentMessage: String? = null

    public var answeredMessageTimestamp: String? = null

    public var answeredMessageText: String? = null

    public lateinit var previousMessageTimestamp: String

    public var previousMessageFailsWithException: Boolean = false

    public var updatedMessageTimestamp: String? = null

    public var updatedMessageText: String? = null

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
