package com.avito.slack

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

/**
 * Сокращаем количество сообщений в канале путем обновления имеющегося сообщения
 * Обновлять или писать новое решает[condition]
 */
class SlackConditionalSender(
    private val slackClient: SlackClient,
    private val updater: SlackMessageUpdater,
    private val condition: SlackMessageUpdateCondition,
    loggerFactory: LoggerFactory
) : SlackMessageSender {

    private val logger = loggerFactory.create<SlackConditionalSender>()

    override fun sendMessage(message: SlackSendMessageRequest): Result<SlackMessage> {
        logger.info(
            "Sending message using SlackConditionalSender, " +
                "trying to find previous message in channel: ${message.id}"
        )
        return slackClient.findMessage(message.id, condition)
            .fold(
                { previousMessage ->
                    logger.info("Previous message found, updating it instead of posting new one")
                    updater.updateMessage(previousMessage, message.text)
                },
                { throwable ->
                    logger.warn(
                        "Previous message not found, " +
                            "posting new one to channel: ${message.id}; " +
                            "message: '${SlackStringFormat.ellipsize(string = message.text, limit = 50)}'",
                        throwable
                    )
                    slackClient.sendMessage(message)
                }
            )
    }
}
