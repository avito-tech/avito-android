package com.avito.android.contracts.platform.analytics

import com.avito.android.clickstream.EventsTracker
import kotlin.time.Duration

internal fun EventsTracker.trackValidationFailed(
    module: String,
    errorMessage: String,
    type: String?,
) {
    val event = NetworkContractsValidationFailedEvent(
        errorMessage = errorMessage,
        type = type.orEmpty(),
        modulePath = module,
    )
    trackEvent(event)
}

public fun EventsTracker.trackValidationDuration(
    duration: Duration,
    modulePath: String,
) {
    val event = NetworkContractsActionDurationEvent(
        actionType = ActionType.VALIDATION,
        duration = duration.inWholeSeconds,
        modulePath = modulePath
    )
    trackEvent(event)
}

internal fun EventsTracker.trackFixationDuration(
    duration: Duration,
) {
    val event = NetworkContractsActionDurationEvent(
        actionType = ActionType.FIXATION,
        duration = duration.inWholeSeconds,
        modulePath = null,
    )
    trackEvent(event)
}
