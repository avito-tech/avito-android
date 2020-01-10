package com.avito.slack

import com.avito.slack.model.FoundMessage

class ConjunctionMessageUpdateCondition(
    private val conditions: List<SlackMessageUpdateCondition>
) : SlackMessageUpdateCondition {

    override fun updateIf(existingMessage: FoundMessage): Boolean = conditions.all { it.updateIf(existingMessage) }
}
