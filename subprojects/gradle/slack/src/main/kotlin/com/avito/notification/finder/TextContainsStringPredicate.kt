package com.avito.notification.finder

import com.avito.notification.model.FoundMessage

public class TextContainsStringPredicate(private val string: String) : NotificationPredicate {

    override fun matches(existingMessage: FoundMessage): Boolean {
        return existingMessage.text.contains(string)
    }
}
