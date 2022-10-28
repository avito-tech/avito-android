package com.avito.alertino.internal.model

import com.google.gson.annotations.SerializedName

internal data class Recipient(
    @SerializedName("addresses") val addresses: List<String>,
    @SerializedName("transport") val transport: Transport = Transport.MATTERMOST,
)
