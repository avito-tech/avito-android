package com.avito.notification

import com.avito.http.HttpClientProvider
import com.avito.slack.SlackNotificationClient

public object NotificationClientFactory {

    public fun createSlackClient(
        serviceName: String,
        token: String,
        workspace: String,
        httpClientProvider: HttpClientProvider
    ): NotificationClient {
        return SlackNotificationClient(serviceName, token, workspace, httpClientProvider)
    }
}
