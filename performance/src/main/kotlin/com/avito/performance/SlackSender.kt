package com.avito.performance

import com.github.seratch.jslack.Slack
import com.github.seratch.jslack.api.model.Attachment
import com.github.seratch.jslack.api.webhook.Payload

interface SlackSender {

    fun sendToSlack(messages: List<String>, color: String)

    class Impl(
        private val slackConfig: SlackConfig,
        private val buildUrl: String? = null
    ) : SlackSender {

        override fun sendToSlack(messages: List<String>, color: String) {
            @Suppress("DEPRECATION")
            Slack.getInstance().send(
                slackConfig.hookUrl,
                Payload.builder()
                    .channel(slackConfig.channel)
                    .text("Build: $buildUrl")
                    .apply {
                        attachments(
                            messages.map {
                                Attachment.builder()
                                    .color(color)
                                    .text(it)
                                    .build()
                            }.toList()

                        )
                    }
                    .username(slackConfig.username)
                    .iconEmoji(slackConfig.avatar)
                    .build()
            )
        }
    }
}
