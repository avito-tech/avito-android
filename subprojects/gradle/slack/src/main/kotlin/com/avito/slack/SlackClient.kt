package com.avito.slack

import com.avito.android.Result
import com.avito.http.HttpClientProvider
import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage

public interface SlackClient : SlackMessageSender, SlackFileUploader {

    public fun updateMessage(
        channel: SlackChannel,
        text: String,
        messageTimestamp: String
    ): Result<SlackMessage>

    public fun findMessage(
        channel: SlackChannel,
        predicate: SlackMessagePredicate
    ): Result<FoundMessage>

    public companion object {

        public fun create(
            serviceName: String,
            token: String,
            workspace: String,
            httpClientProvider: HttpClientProvider
        ): SlackClient {
            return SlackClientImpl(serviceName, token, workspace, httpClientProvider)
        }
    }
}
