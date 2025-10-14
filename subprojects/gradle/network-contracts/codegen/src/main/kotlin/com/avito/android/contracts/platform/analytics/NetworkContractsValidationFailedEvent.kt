package com.avito.android.contracts.platform.analytics

import com.avito.android.clickstream.ClickStreamSrcId
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.clickstream.event.ParametrizedClickStreamEvent

internal data class NetworkContractsValidationFailedEvent(
    val errorMessage: String,
    val type: String,
    val modulePath: String,
    val kind: String,
    val variantName: String,
) : ClickStreamEvent by ParametrizedClickStreamEvent(
    eventId = 16653,
    srcId = ClickStreamSrcId.NETWORK_CONTRACTS,
    version = 7,
    params = mapOf(
        "error_message" to errorMessage,
        "nc_validation_failed_type" to type,
        "module_path" to modulePath,
        "kind" to kind,
        "nc_generator" to variantName,
    ),
)
