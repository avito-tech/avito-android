package com.avito.slack

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

/**
 * Чтобы не упустить из-за чего обновлено сообщение в канале,
 * дополнительно пишем в тред к этому сообщению информацию об обновлении[threadMessage]
 */
class SlackMessageUpdaterWithThreadMark(
    private val slackClient: SlackClient,
    loggerFactory: LoggerFactory,
    private val threadMessage: String
) : SlackMessageUpdater {

    private val logger = loggerFactory.create<SlackMessageUpdaterWithThreadMark>()

    override fun updateMessage(
        previousMessage: FoundMessage,
        newContent: String
    ): Result<SlackMessage> {
        logger.info(
            "Updating message with thread mark; channel: ${previousMessage.channel.name}; " +
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
                "Thread message posted; channel: ${previousMessage.channel.name}; " +
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
                    "Original message updated; " +
                        "newMessage: '${SlackStringFormat.ellipsize(string = newContent, limit = 50)}'"
                )
            }
    }
}
