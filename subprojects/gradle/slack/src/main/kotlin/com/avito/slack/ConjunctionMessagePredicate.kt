package com.avito.slack

import com.avito.slack.model.FoundMessage

public class ConjunctionMessagePredicate(
    private val conditions: List<SlackMessagePredicate>
) : SlackMessagePredicate {

    override fun matches(existingMessage: FoundMessage): Boolean = conditions.all { it.matches(existingMessage) }
}
