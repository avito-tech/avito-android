package com.avito.slack

import com.avito.android.Result
import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

public class SlackMessageUpdaterDirectlyToThread(
    private val slackClient: SlackClient,
) : SlackMessageUpdater {

    override fun updateMessage(previousMessage: FoundMessage, newContent: String): Result<SlackMessage> {
        return slackClient.sendMessage(
            SlackSendMessageRequest(
                channel = previousMessage.channel,
                text = newContent,
                author = previousMessage.author,
                emoji = previousMessage.emoji,
                threadId = previousMessage.timestamp
            )
        )
    }
}
