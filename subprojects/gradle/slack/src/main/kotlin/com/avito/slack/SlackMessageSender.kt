package com.avito.slack

import com.avito.android.Result
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

interface SlackMessageSender {

    fun sendMessage(message: SlackSendMessageRequest): Result<SlackMessage>
}
