package com.avito.slack.model

fun FoundMessage.Companion.createStubInstance(
    text: String = "",
    botId: String = "",
    timestamp: String = "",
    emoji: String? = null,
    author: String = "",
    channel: SlackChannel = SlackChannel("#")
) = FoundMessage(text = text, botId = botId, timestamp = timestamp, author = author, channel = channel, emoji = emoji)
