package com.avito.slack.model

import java.io.Serializable

/**
 * id should be provided because there are no way to get channel id from channel name via slack api
 *
 * see https://stackoverflow.com/a/50114874
 *
 * name should be provided for:
 *  - readability: it's easier to know what is the target channel just by looking at config file
 *  - consistency: channel id could be changed in some cases, it should point at the same name
 */
data class SlackChannel(
    val id: String,
    val name: String
) : Serializable {

    init {
        require(name.startsWith("#") || name.startsWith("@")) {
            "Channel name must starts with # or @"
        }
    }

    companion object
}
