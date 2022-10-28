package com.avito.alertino.internal.model

import com.google.gson.annotations.SerializedName

internal data class SendNotificationResponse(
    @SerializedName("result") val result: SendNotificationResult
) {

    internal data class SendNotificationResult(
        @SerializedName("createdMessages") val createdMessages: Map<String, String>,
        @SerializedName("alreadyExistingMessages") val alreadyExistingMessages: Map<String, String>,
        @SerializedName("creationErrors") val creationErrors: Map<String, String>,
    )
}
