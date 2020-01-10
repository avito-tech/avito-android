package com.avito.slack

import com.avito.slack.model.FoundMessage

interface SlackMessageUpdateCondition {

    fun updateIf(existingMessage: FoundMessage): Boolean
}
