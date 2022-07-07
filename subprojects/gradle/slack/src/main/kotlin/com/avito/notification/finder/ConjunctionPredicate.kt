package com.avito.notification.finder

import com.avito.notification.model.FoundMessage

public class ConjunctionPredicate(
    private val conditions: List<NotificationPredicate>
) : NotificationPredicate {

    override fun matches(existingMessage: FoundMessage): Boolean = conditions.all { it.matches(existingMessage) }
}
