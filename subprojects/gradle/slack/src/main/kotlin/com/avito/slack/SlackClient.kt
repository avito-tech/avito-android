package com.avito.slack

import com.avito.android.Result
import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.github.seratch.jslack.Slack
import com.github.seratch.jslack.api.methods.MethodsClient
import com.github.seratch.jslack.api.methods.SlackApiResponse
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest
import com.github.seratch.jslack.api.methods.request.conversations.ConversationsHistoryRequest
import java.io.File

interface SlackClient : SlackMessageSender, SlackFileUploader {

    fun updateMessage(
        channel: SlackChannel,
        text: String,
        messageTimestamp: String
    ): Result<SlackMessage>

    fun findMessage(
        channel: SlackChannel,
        predicate: SlackMessagePredicate
    ): Result<FoundMessage>

    class Impl(private val token: String, private val workspace: String) : SlackClient {

        private val methodsClient: MethodsClient = Slack.getInstance().methods()

        /**
         * fetch [messagesLookupCount] messages back though history
         */
        private val messagesLookupCount = 25

        override fun sendMessage(message: SlackSendMessageRequest): Result<SlackMessage> {

            val requestBuilder = ChatPostMessageRequest.builder()
                .token(token)
                .channel(message.channel.id)
                .text(message.text)
                .username(message.author)

            if (message.emoji != null) requestBuilder.iconEmoji(message.emoji)

            if (message.threadId != null) requestBuilder.threadTs(message.threadId)

            val request = requestBuilder.build()
            return methodsClient.chatPostMessage(request)
                .toResult()
                .map { response ->
                    SlackMessage(
                        workspace = workspace,
                        id = response.ts,
                        text = response.message?.text ?: "",
                        channelId = response.channel,
                        author = response.message.username,
                        threadId = response.message.threadTs
                    )
                }
        }

        override fun uploadHtml(channel: SlackChannel, message: String, file: File): Result<Unit> {
            return methodsClient.filesUpload {
                it.file(file)
                it.channels(listOf(channel.id))
                it.filename(file.name)
                it.initialComment(message)
                it.token(token)
            }.toResult().map { }
        }

        override fun updateMessage(
            channel: SlackChannel,
            text: String,
            messageTimestamp: String
        ): Result<SlackMessage> {

            val request = ChatUpdateRequest.builder()
                .token(token)
                .channel(channel.id)
                .ts(messageTimestamp)
                .text(text)
                .build()

            return methodsClient.chatUpdate(request).toResult()
                .map { message ->
                    SlackMessage(
                        workspace = workspace,
                        id = message.ts,
                        text = message.text,
                        channelId = message.channel,
                        author = message.message.username
                    )
                }
        }

        override fun findMessage(
            channel: SlackChannel,
            predicate: SlackMessagePredicate
        ): Result<FoundMessage> {

            val request = ConversationsHistoryRequest.builder()
                .token(token)
                .limit(messagesLookupCount)
                .channel(channel.id)
                .build()

            return methodsClient.conversationsHistory(request)
                .toResult()
                .map { response ->
                    response.messages.asSequence()
                        .map { message ->
                            FoundMessage(
                                timestamp = message.ts,
                                text = message.text,
                                botId = message.botId,
                                author = message.username,
                                channel = channel,
                                emoji = message.icons?.emoji
                            )
                        }
                        .find { slackMessage -> predicate.matches(slackMessage) }
                        ?: throw Exception("Message that satisfies provided predicate not found")
                }
        }
    }
}

private fun <T : SlackApiResponse> T.toResult(): Result<T> {
    return if (isOk) {
        Result.Success(this)
    } else {
        Result.Failure(RuntimeException("Slack request failed; error=$error [needed=$needed; provided=$provided]"))
    }
}
