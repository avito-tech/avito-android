package com.avito.slack

import com.avito.slack.model.FoundMessage

class TextContainsStringCondition(private val string: String) : SlackMessageUpdateCondition {

    override fun updateIf(existingMessage: FoundMessage): Boolean {
        return existingMessage.text.contains(string)
    }
}
