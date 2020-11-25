package com.avito.slack

import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.utils.logging.CILogger
import org.funktionale.tries.Try

/**
 * Сокращаем количество сообщений в канале путем обновления имеющегося сообщения
 * Обновлять или писать новое решает[condition]
 */
class SlackConditionalSender(
    private val slackClient: SlackClient,
    private val updater: SlackMessageUpdater,
    private val condition: SlackMessageUpdateCondition,
    private val logger: CILogger
) : SlackMessageSender {

    override fun sendMessage(message: SlackSendMessageRequest): Try<SlackMessage> {
        logger.info(
            "[Slack] Sending message using SlackConditionalSender, " +
                "trying to find previous message in channel=${message.channel}"
        )
        return slackClient.findMessage(message.channel, condition)
            .fold(
                { previousMessage ->
                    logger.info("[Slack] Previous message found, updating it instead of posting new one")
                    updater.updateMessage(previousMessage, message.text)
                },
                { throwable ->
                    logger.info(
                        message = "[Slack] Previous message not found, " +
                            "posting new one in channel=${message.channel}; " +
                            "message=${SlackStringFormat.ellipsize(string = message.text, limit = 50)}",
                        error = throwable
                    )
                    slackClient.sendMessage(message)
                }
            )
    }
}
