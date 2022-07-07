package com.avito.slack

import com.avito.android.Result
import com.avito.notification.model.FoundMessage
import com.avito.slack.model.SlackMessage

public interface SlackMessageUpdater {

    public fun updateMessage(previousMessage: FoundMessage, newContent: String): Result<SlackMessage>
}
