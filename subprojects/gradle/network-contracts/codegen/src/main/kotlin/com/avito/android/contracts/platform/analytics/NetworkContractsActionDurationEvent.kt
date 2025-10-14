package com.avito.android.contracts.platform.analytics

import com.avito.android.clickstream.ClickStreamSrcId
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.clickstream.event.ParametrizedClickStreamEvent

public data class NetworkContractsActionDurationEvent(
    val actionType: ActionType,
    val duration: Long,
    val modulePath: String?,
    val kind: String,
    val varinantName: String,
) : ClickStreamEvent by ParametrizedClickStreamEvent(
    eventId = 16650,
    version = 4,
    srcId = ClickStreamSrcId.NETWORK_CONTRACTS,
    params = mapOf(
        "nc_action_duration" to duration,
        "action_type" to actionType.value,
        "module_path" to modulePath.orEmpty(),
        "kind" to kind,
        "nc_generator" to varinantName,
    ),
)
