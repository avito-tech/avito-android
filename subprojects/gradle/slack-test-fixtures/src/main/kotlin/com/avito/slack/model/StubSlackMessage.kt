package com.avito.slack.model

fun SlackMessage.Companion.createStubInstance(
    workspace: String = "",
    timestamp: String = "",
    message: String = "",
    channelId: String = "",
    author: String = ""
) = SlackMessage(
    workspace = workspace,
    id = timestamp,
    text = message,
    channelId = channelId,
    author = author
)
