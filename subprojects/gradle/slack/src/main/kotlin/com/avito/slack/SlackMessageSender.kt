package com.avito.slack

import com.avito.android.Result
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

public interface SlackMessageSender {

    public fun sendMessage(message: SlackSendMessageRequest): Result<SlackMessage>
}
