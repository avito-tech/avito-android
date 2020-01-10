package com.avito.slack.model

data class SlackChannel(val name: String) {

    init {
        require(name.startsWith("#") || name.startsWith("@")) { "Channel name should start with # or @" }
    }
}

internal val SlackChannel.strippedName: String
    get() = this.name
        .removePrefix("#")
        .removePrefix("@")
