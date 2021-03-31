package com.avito.slack

import com.avito.slack.model.FoundMessage

class SameAuthorPredicate(private val author: String) : SlackMessagePredicate {

    override fun matches(existingMessage: FoundMessage): Boolean {
        return author == existingMessage.author
    }
}
