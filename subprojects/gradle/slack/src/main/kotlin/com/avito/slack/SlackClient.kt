@file:Suppress("DEPRECATION")
//todo use new slack api

package com.avito.slack

import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest
import com.avito.slack.model.strippedName
import com.github.seratch.jslack.Slack
import com.github.seratch.jslack.api.methods.MethodsClient
import com.github.seratch.jslack.api.methods.SlackApiResponse
import com.github.seratch.jslack.api.methods.request.channels.ChannelsHistoryRequest
import com.github.seratch.jslack.api.methods.request.channels.ChannelsListRequest
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest
import com.github.seratch.jslack.api.model.Channel
import org.funktionale.tries.Try
import java.io.File

interface SlackClient : SlackMessageSender, SlackFileUploader {

    fun updateMessage(
        channel: SlackChannel,
        text: String,
        messageTimestamp: String
    ): Try<SlackMessage>

    fun findMessage(channel: SlackChannel, predicate: SlackMessageUpdateCondition): Try<FoundMessage>

    class Impl(private val token: String, private val workspace: String) : SlackClient {

        private val methodsClient: MethodsClient = Slack.getInstance().methods()

        /**
         * для [findMessage] фетчим [messagesLookupCount] сообщений вверх, число просто взято "с запасом"
         */
        private val messagesLookupCount = 100

        override fun sendMessage(message: SlackSendMessageRequest): Try<SlackMessage> {

            val requestBuilder = ChatPostMessageRequest.builder()
                .token(token)
                .channel(message.channel.name)
                .text(message.text)
                .username(message.author)

            if (message.emoji != null) requestBuilder.iconEmoji(message.emoji)

            if (message.threadId != null) requestBuilder.threadTs(message.threadId)

            val request = requestBuilder.build()
            return methodsClient.chatPostMessage(request)
                .toTry()
                .map { response ->
                    SlackMessage(
                        workspace = workspace,
                        id = response.ts,
                        text = response.message?.text ?: "",
                        channel = message.channel,
                        channelId = response.channel,
                        author = response.message.username,
                        threadId = response.message.threadTs
                    )
                }
        }

        override fun uploadHtml(channel: SlackChannel, message: String, file: File): Try<Unit> {
            return findChannelByName(channel.strippedName).flatMap { channelInfo ->
                methodsClient.filesUpload {
                    it.file(file)
                    it.channels(listOf(channelInfo.id))
                    it.filename(file.name)
                    it.initialComment(message)
                    it.token(token)
                }.toTry()
            }.map { Unit }
        }

        override fun updateMessage(
            channel: SlackChannel,
            text: String,
            messageTimestamp: String
        ): Try<SlackMessage> {
            return findChannelByName(channel.strippedName)
                .flatMap { channelInfo ->

                    val request = ChatUpdateRequest.builder()
                        .token(token)
                        .channel(channelInfo.id)
                        .ts(messageTimestamp)
                        .text(text)
                        .build()

                    methodsClient.chatUpdate(request).toTry()

                }.map {
                    SlackMessage(
                        workspace = workspace,
                        id = it.ts,
                        text = it.text,
                        channel = channel,
                        channelId = it.channel,
                        author = it.message.username
                    )
                }
        }

        override fun findMessage(channel: SlackChannel, predicate: SlackMessageUpdateCondition): Try<FoundMessage> {
            return findChannelByName(channel.strippedName)
                .map { channelInfo ->
                    ChannelsHistoryRequest.builder()
                        .token(token)
                        .count(messagesLookupCount)
                        .channel(channelInfo.id)
                        .build()
                }.flatMap { request ->
                    methodsClient.channelsHistory(request)
                        .toTry()
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
                                .find { slackMessage -> predicate.updateIf(slackMessage) }
                                ?: throw Exception("Message that satisfies provided predicate not found")
                        }
                }
        }

        // Ищем ID канала по его имени
        // Судя по https://stackoverflow.com/a/50114874/2893307 это единственный способ
        private fun findChannelByName(name: String): Try<Channel> {
            val request = ChannelsListRequest.builder()
                .token(token)
                .excludeArchived(true)
                .build()

            @Suppress("RemoveExplicitTypeArguments") // Type inference failed for flatMap<Channel>
            return methodsClient.channelsList(request)
                .toTry()
                .map { response ->
                    response.channels.find { channel -> channel.name == name }
                }
                .flatMap<Channel> { channel ->
                    if (channel != null) {
                        Try.Success(channel)
                    } else {
                        Try.Failure(IllegalArgumentException("Cannot find channel with name $name"))
                    }
                }
        }
    }
}

private fun <T : SlackApiResponse> T.toTry(): Try<T> {
    return if (this.isOk) {
        Try.Success(this)
    } else {
        Try.Failure(RuntimeException("Slack request failed; error=${this.error} [needed=${this.needed}; provided=${this.provided}]"))
    }
}
