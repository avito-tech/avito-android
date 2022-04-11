package com.avito.notification

import com.avito.android.Result
import com.avito.notification.finder.NotificationFinder
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.SlackMessage

public interface NotificationClient : NotificationSender, NotificationFinder {

    public fun updateMessage(
        channel: SlackChannel,
        text: String,
        messageTimestamp: String
    ): Result<SlackMessage>
}
