package com.avito.alertino.internal.model

import com.google.gson.annotations.SerializedName

internal data class SendNotificationBody(
    @SerializedName("notificationName") val template: String,
    @SerializedName("recipients") val recipients: List<Recipient>,
    @SerializedName("subscribeRecipients") val subscribeRecipients: Boolean,
    @SerializedName("labels") val labels: List<String>,
    // Base64 encoded string with valid JSON object
    @SerializedName("values") val values: String,
)
