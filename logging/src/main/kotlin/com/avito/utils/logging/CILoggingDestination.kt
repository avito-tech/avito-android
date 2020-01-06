package com.avito.utils.logging

import com.avito.android.sentry.SentryConfig
import com.avito.android.sentry.sentryClient
import com.github.seratch.jslack.Slack
import com.github.seratch.jslack.api.model.Attachment
import com.github.seratch.jslack.api.webhook.Payload
import io.sentry.SentryClient

class SlackDestination(
    private val hookUrl: String,
    private val channel: String,
    private val username: String,
    private val avatar: String
) : CILoggingDestination() {

    override fun write(formattedMessage: CILoggingFormatter.FormattedMessage) {
        try {
            @Suppress("DEPRECATION")
            Slack.getInstance().send(
                hookUrl,
                Payload.builder()
                    .channel(channel)
                    .text(formattedMessage.message)
                    .apply {
                        if (formattedMessage.details != null) {
                            attachments(
                                listOf(
                                    Attachment.builder()
                                        .color(ATTACHMENT_COLOR)
                                        .text(formattedMessage.details)
                                        .build()
                                )
                            )
                        }
                    }
                    .username(username)
                    .iconEmoji(avatar)
                    .build()
            )
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun child(tag: String): CILoggingDestination = this

    companion object {
        private const val ATTACHMENT_COLOR = "A30200"
    }
}

class SentryDestination(private val config: SentryConfig) : CILoggingDestination() {

    @Transient
    private lateinit var _sentry: SentryClient

    private fun sentry(): SentryClient {
        if (!::_sentry.isInitialized) {
            _sentry = sentryClient(config)
        }
        return _sentry
    }

    override fun write(formattedMessage: CILoggingFormatter.FormattedMessage) {
        if (formattedMessage.cause != null) {
            sentry().sendException(formattedMessage.cause)
        }
    }

    override fun child(tag: String): CILoggingDestination = this
}
