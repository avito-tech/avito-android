package com.avito.slack

import com.avito.slack.model.FoundMessage

public interface SlackMessagePredicate {

    public fun matches(existingMessage: FoundMessage): Boolean
}
