package com.avito.slack

import com.avito.android.Result
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

/**
 * Сокращаем количество сообщений в канале путем обновления имеющегося сообщения
 * Обновлять или писать новое решает[condition]
 */
public class SlackConditionalSender(
    private val slackClient: SlackClient,
    private val updater: SlackMessageUpdater,
    private val condition: SlackMessagePredicate,
) : SlackMessageSender {

    override fun sendMessage(message: SlackSendMessageRequest): Result<SlackMessage> {
        return slackClient.findMessage(message.channel, condition)
            .fold(
                { previousMessage ->
                    updater.updateMessage(previousMessage, message.text)
                },
                {
                    // TODO handle throwable
                    slackClient.sendMessage(message)
                }
            )
    }
}
