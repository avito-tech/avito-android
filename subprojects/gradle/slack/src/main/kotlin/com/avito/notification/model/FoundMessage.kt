package com.avito.notification.model

import com.avito.slack.model.SlackChannel

public data class FoundMessage(
    val text: String,
    val botId: String?,
    val timestamp: String,
    val author: String?,
    val emoji: String?,
    val channel: SlackChannel
) {
    public companion object
}
