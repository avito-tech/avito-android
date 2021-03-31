package com.avito.slack

import com.avito.slack.model.FoundMessage

interface SlackMessagePredicate {

    fun matches(existingMessage: FoundMessage): Boolean
}
