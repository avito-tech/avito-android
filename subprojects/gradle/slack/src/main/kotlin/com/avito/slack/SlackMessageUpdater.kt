package com.avito.slack

import com.avito.slack.model.FoundMessage
import com.avito.slack.model.SlackMessage
import org.funktionale.tries.Try

interface SlackMessageUpdater {

    fun updateMessage(previousMessage: FoundMessage, newContent: String): Try<SlackMessage>
}
