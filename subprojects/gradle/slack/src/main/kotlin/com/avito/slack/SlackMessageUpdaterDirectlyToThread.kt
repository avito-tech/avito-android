package com.avito.slack

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

class SlackMessageUpdaterDirectlyToThread(
    private val slackClient: SlackClient,
    loggerFactory: LoggerFactory
) : SlackMessageUpdater {

    private val logger = loggerFactory.create<SlackMessageUpdaterDirectlyToThread>()

    override fun updateMessage(previousMessage: FoundMessage, newContent: String): Result<SlackMessage> {
        logger.info(
            "Updating message by posting to its thread; channel=${previousMessage.channel}; " +
                "oldMessage=${SlackStringFormat.ellipsize(string = previousMessage.text, limit = 50)}; "
        )

        return slackClient.sendMessage(
            SlackSendMessageRequest(
                id = previousMessage.channel,
                text = newContent,
                author = previousMessage.author,
                emoji = previousMessage.emoji,
                threadId = previousMessage.timestamp
            )
        ).onSuccess {
            logger.info(
                "Update message posted to thread; " +
                    "message=${SlackStringFormat.ellipsize(string = newContent, limit = 50)}"
            )
        }
    }
}
