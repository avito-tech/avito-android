package com.avito.slack.model

data class SlackSendMessageRequest(
    val id: SlackChannel,
    val text: String,
    val author: String?,
    val emoji: String? = null,
    val threadId: String? = null
) {

    override fun toString(): String = "Message "
}
