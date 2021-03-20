package com.avito.slack

import com.avito.android.Result
import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackMessage

interface SlackMessageUpdater {

    fun updateMessage(previousMessage: FoundMessage, newContent: String): Result<SlackMessage>
}
