package com.avito.slack.model

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * @param id он же timestamp, он же ts https://github.com/slackhq/slack-api-docs/issues/7
 * @param threadId это [id] сообщения к которому открыт тред, тоже в таком же формате как id
 */
data class SlackMessage(
    val workspace: String,
    val id: String,
    val text: String,
    val channel: SlackChannel,
    val channelId: String,
    val author: String,
    val threadId: String? = null
) {
    val link: HttpUrl
        get() {
            val source = StringBuilder("https://$workspace.slack.com/archives/$channelId/p$id")
            if (threadId != null) {
                source.append("?thread_ts=$threadId&cid=$channelId")
            }
            return source.toString().toHttpUrl()
        }

    companion object
}
