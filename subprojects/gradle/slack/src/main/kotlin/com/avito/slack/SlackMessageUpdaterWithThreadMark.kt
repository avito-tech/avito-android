package com.avito.slack

import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.utils.logging.CILogger
import org.funktionale.tries.Try

/**
 * Чтобы не упустить из-за чего обновлено сообщение в канале,
 * дополнительно пишем в тред к этому сообщению информацию об обновлении[threadMessage]
 */
class SlackMessageUpdaterWithThreadMark(
    private val slackClient: SlackClient,
    private val logger: CILogger,
    private val threadMessage: String
) : SlackMessageUpdater {

    override fun updateMessage(
        previousMessage: FoundMessage,
        newContent: String
    ): Try<SlackMessage> {
        logger.info(
            "[Slack] Updating message with thread mark; channel: ${previousMessage.channel.name}; " +
                "oldMessage: '${SlackStringFormat.ellipsize(string = previousMessage.text, limit = 50)}'; "
        )
        return slackClient.sendMessage(
            SlackSendMessageRequest(
                channel = previousMessage.channel,
                text = threadMessage,
                author = previousMessage.author,
                emoji = previousMessage.emoji,
                threadId = previousMessage.timestamp
            )
        ).flatMap {
            logger.info(
                "[Slack] Thread message posted; channel: ${previousMessage.channel.name}; " +
                    "threadId: '${previousMessage.timestamp}'; " +
                    SlackStringFormat.ellipsize(string = threadMessage, limit = 50)
            )

            slackClient.updateMessage(
                channel = previousMessage.channel,
                text = newContent,
                messageTimestamp = previousMessage.timestamp
            )
        }
            .onSuccess {
                logger.info(
                    "[Slack] Original message updated; " +
                        "newMessage: '${SlackStringFormat.ellipsize(string = newContent, limit = 50)}'"
                )
            }
    }
}
