package com.avito.notification

import com.avito.android.Result
import com.avito.slack.model.SlackMessage
import com.avito.slack.model.SlackSendMessageRequest

public interface NotificationSender {

    public fun sendMessage(message: SlackSendMessageRequest): Result<SlackMessage>
}
