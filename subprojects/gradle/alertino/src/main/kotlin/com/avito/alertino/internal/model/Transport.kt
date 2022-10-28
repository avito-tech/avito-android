package com.avito.alertino.internal.model

import com.google.gson.annotations.SerializedName

internal enum class Transport {
    @SerializedName("slack") SLACK,
    @SerializedName("mattermost") MATTERMOST,
}
