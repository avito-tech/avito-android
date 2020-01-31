package com.avito.slack

import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.utils.logging.CILogger
import org.funktionale.tries.Try

class SlackMessageUpdaterDirectlyToThread(
    private val slackClient: SlackClient,
    private val logger: CILogger
) : SlackMessageUpdater {

    override fun updateMessage(previousMessage: FoundMessage, newContent: String): Try<SlackMessage> {
        logger.info(
            "[Slack] Updating message by posting to its thread; channel=${previousMessage.channel}; " +
                "oldMessage=${SlackStringFormat.ellipsize(string = previousMessage.text, limit = 50)}; "
        )

        return slackClient.sendMessage(
            SlackSendMessageRequest(
                channel = previousMessage.channel,
                text = newContent,
                author = previousMessage.author,
                emoji = previousMessage.emoji,
                threadId = previousMessage.timestamp
            )
        ).onSuccess {
            logger.info(
                "[Slack] Update message posted to thread; " +
                    "message=${SlackStringFormat.ellipsize(string = newContent, limit = 50)}"
            )
        }
    }
}
