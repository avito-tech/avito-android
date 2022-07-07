package com.avito.slack

import com.avito.android.Result
import com.avito.notification.NotificationClient
import com.avito.notification.NotificationSender
import com.avito.notification.finder.NotificationPredicate
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

/**
 * Сокращаем количество сообщений в канале путем обновления имеющегося сообщения
 * Обновлять или писать новое решает[condition]
 */
public class SlackConditionalSender(
    private val notificationClient: NotificationClient,
    private val updater: SlackMessageUpdater,
    private val condition: NotificationPredicate,
) : NotificationSender {

    override fun sendMessage(message: SlackSendMessageRequest): Result<SlackMessage> {
        return notificationClient.findMessage(message.channel, condition)
            .fold(
                { previousMessage ->
                    updater.updateMessage(previousMessage, message.text)
                },
                {
                    // TODO handle throwable
                    notificationClient.sendMessage(message)
                }
            )
    }
}
