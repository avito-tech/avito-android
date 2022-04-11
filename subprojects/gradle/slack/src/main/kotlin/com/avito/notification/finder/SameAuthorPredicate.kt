package com.avito.notification.finder

import com.avito.notification.model.FoundMessage

public class SameAuthorPredicate(private val author: String) : NotificationPredicate {

    override fun matches(existingMessage: FoundMessage): Boolean {
        return author == existingMessage.author
    }
}
