package com.avito.slack

import com.avito.slack.model.FoundMessage

class SameAuthorUpdateCondition(private val author: String) : SlackMessageUpdateCondition {

    override fun updateIf(existingMessage: FoundMessage): Boolean {
        return author == existingMessage.author
    }
}
