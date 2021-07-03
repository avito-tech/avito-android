package com.avito.slack

import com.avito.slack.model.FoundMessage

public class TextContainsStringCondition(private val string: String) : SlackMessagePredicate {

    override fun matches(existingMessage: FoundMessage): Boolean {
        return existingMessage.text.contains(string)
    }
}
