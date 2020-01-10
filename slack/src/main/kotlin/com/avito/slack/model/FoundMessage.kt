package com.avito.slack.model

data class FoundMessage(
    val text: String,
    val botId: String,
    val timestamp: String,
    val author: String,
    val emoji: String?,
    val channel: SlackChannel
) {
    companion object
}
