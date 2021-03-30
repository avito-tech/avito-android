package com.avito.slack.model

fun SlackChannel.Companion.createStubInstance(
    id: String = "C01D88JT6CX",
    name: String = "#android-integration-test"
): SlackChannel =
    SlackChannel(id = id, name = name)
