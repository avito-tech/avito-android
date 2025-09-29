package com.avito.android.network_contracts.analytics

import com.avito.android.clickstream.ClickStreamSrcId
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.clickstream.event.ParametrizedClickStreamEvent

internal data class NetworkContractsValidationFailedEvent(
    val errorMessage: String,
    val type: String,
    val modulePath: String,
) : ClickStreamEvent by ParametrizedClickStreamEvent(
    eventId = 16653,
    srcId = ClickStreamSrcId.NETWORK_CONTRACTS,
    version = 5,
    params = mapOf(
        "error_message" to errorMessage,
        "nc_validation_failed_type" to type,
        "module_path" to modulePath,
    ),
)
