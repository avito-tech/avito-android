package com.avito.notification.finder

import com.avito.android.Result
import com.avito.notification.model.FoundMessage
import com.avito.slack.model.SlackChannel

public interface NotificationFinder {

    public fun findMessage(
        channel: SlackChannel,
        predicate: NotificationPredicate
    ): Result<FoundMessage>
}
