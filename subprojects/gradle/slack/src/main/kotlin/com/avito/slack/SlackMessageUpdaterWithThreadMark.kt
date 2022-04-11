package com.avito.slack

import com.avito.android.Result
import com.avito.notification.NotificationClient
import com.avito.notification.model.FoundMessage
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

/**
 * Чтобы не упустить из-за чего обновлено сообщение в канале,
 * дополнительно пишем в тред к этому сообщению информацию об обновлении[threadMessage]
 */
public class SlackMessageUpdaterWithThreadMark(
    private val notificationClient: NotificationClient,
    private val threadMessage: String
) : SlackMessageUpdater {

    override fun updateMessage(
        previousMessage: FoundMessage,
        newContent: String
    ): Result<SlackMessage> {
        return notificationClient.sendMessage(
            SlackSendMessageRequest(
                channel = previousMessage.channel,
                text = threadMessage,
                author = previousMessage.author,
                emoji = previousMessage.emoji,
                threadId = previousMessage.timestamp
            )
        ).flatMap {
            notificationClient.updateMessage(
                channel = previousMessage.channel,
                text = newContent,
                messageTimestamp = previousMessage.timestamp
            )
        }
    }
}
