package com.avito.alertino.internal.model

import com.google.gson.annotations.SerializedName

internal data class SendNotificationToThreadBody(
    @SerializedName("notificationName") val template: String,
    // E.g. {"@username", "thread-id"}
    @SerializedName("recipientsAndSyntheticThreadTs") val recipientToThreadIdMap: Map<String, String>,
    // Base64 encoded string with valid JSON object
    @SerializedName("values") val values: String,
)
