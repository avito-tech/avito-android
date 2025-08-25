package com.avito.android.clickstream.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ClickStreamEventRequest(
    @SerialName("meta") val meta: NetworkClickStreamMeta,
    @SerialName("events") val events: List<NetworkClickStreamEvent>,
)

@Serializable
public data class NetworkClickStreamEvent(
    @SerialName("event_id") val eventId: Int,
    @SerialName("version") val version: Int,
    @SerialName("env") val env: Map<String, String>,
    @SerialName("params") val params: Map<String, String>,
)

@Serializable
public data class NetworkClickStreamMeta(
    @SerialName("sdk") val sdk: String,
    @SerialName("build_uid") val buildUid: String,
    @SerialName("src_id") val srcId: Int,
)
