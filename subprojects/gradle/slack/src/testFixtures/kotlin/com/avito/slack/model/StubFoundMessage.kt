package com.avito.slack.model

public fun FoundMessage.Companion.createStubInstance(
    text: String = "",
    botId: String? = null,
    timestamp: String = "",
    emoji: String? = null,
    author: String? = null,
    channel: SlackChannel = SlackChannel.createStubInstance()
): FoundMessage = FoundMessage(
    text = text,
    botId = botId,
    timestamp = timestamp,
    author = author,
    channel = channel,
    emoji = emoji
)
