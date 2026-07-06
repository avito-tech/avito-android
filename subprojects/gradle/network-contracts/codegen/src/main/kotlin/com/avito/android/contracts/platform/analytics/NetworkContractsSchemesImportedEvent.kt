package com.avito.android.contracts.platform.analytics

import com.avito.android.clickstream.ClickStreamSrcId
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.clickstream.event.ParametrizedClickStreamEvent

internal data class NetworkContractsSchemesImportedEvent(
    val status: String,
    val errorMessage: String?,
) : ClickStreamEvent by ParametrizedClickStreamEvent(
    eventId = 16436,
    version = 2,
    srcId = ClickStreamSrcId.NETWORK_CONTRACTS,
    params = buildMap {
        put("status_message", status)
        put("dev_tool_name", "gradle")
        if (errorMessage != null) put("error_message", errorMessage)
    },
)
