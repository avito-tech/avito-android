package com.avito.slack.model

fun FoundMessage.Companion.createStubInstance(
    text: String = "",
    botId: String? = null,
    timestamp: String = "",
    emoji: String? = null,
    author: String? = null,
    channelId: SlackChannelId = SlackChannelId("C01D88JT6CX")
) = FoundMessage(
    text = text,
    botId = botId,
    timestamp = timestamp,
    author = author,
    channelId = channelId,
    emoji = emoji
)
