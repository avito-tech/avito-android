package com.avito.slack.model

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
