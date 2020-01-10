package com.avito.slack

import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import org.funktionale.tries.Try

interface SlackMessageSender {

    fun sendMessage(message: SlackSendMessageRequest): Try<SlackMessage>
}
