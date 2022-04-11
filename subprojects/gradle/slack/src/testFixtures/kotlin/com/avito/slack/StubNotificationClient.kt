package com.avito.slack

import com.avito.android.Result
import com.avito.notification.NotificationClient
import com.avito.notification.finder.NotificationPredicate
import com.avito.notification.model.FoundMessage
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.slack.model.createStubInstance

public class StubNotificationClient : NotificationClient {

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
        predicate: NotificationPredicate
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
}
