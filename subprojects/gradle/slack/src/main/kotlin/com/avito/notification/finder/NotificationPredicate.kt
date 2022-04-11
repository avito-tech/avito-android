package com.avito.notification.finder

import com.avito.notification.model.FoundMessage

public interface NotificationPredicate {

    public fun matches(existingMessage: FoundMessage): Boolean
}
