package com.avito.android.contracts.platform.analytics

import com.avito.android.clickstream.EventsTracker
import kotlin.time.Duration

internal fun EventsTracker.trackValidationFailed(
    module: String,
    errorMessage: String,
    type: String?,
    kind: String?,
    variantName: String?,
) {
    val event = NetworkContractsValidationFailedEvent(
        errorMessage = errorMessage,
        type = type.orEmpty(),
        modulePath = module,
        kind = kind.orEmpty(),
        variantName = variantName.orEmpty(),
    )
    trackEvent(event)
}

public fun EventsTracker.trackValidationDuration(
    duration: Duration,
    modulePath: String,
    kind: String?,
    variantName: String?,
) {
    val event = NetworkContractsActionDurationEvent(
        actionType = ActionType.VALIDATION,
        duration = duration.inWholeSeconds,
        modulePath = modulePath,
        kind = kind.orEmpty(),
        varinantName = variantName.orEmpty(),
    )
    trackEvent(event)
}

internal fun EventsTracker.trackFixationDuration(
    duration: Duration,
    kind: String?,
    variantName: String?,
) {
    val event = NetworkContractsActionDurationEvent(
        actionType = ActionType.FIXATION,
        duration = duration.inWholeSeconds,
        modulePath = null,
        kind = kind.orEmpty(),
        varinantName = variantName.orEmpty(),
    )
    trackEvent(event)
}

internal fun EventsTracker.trackSchemesImported(
    success: Boolean,
    errorMessage: String?,
) {
    trackEvent(
        NetworkContractsSchemesImportedEvent(
            status = if (success) "success" else "failure",
            errorMessage = errorMessage,
        )
    )
}
