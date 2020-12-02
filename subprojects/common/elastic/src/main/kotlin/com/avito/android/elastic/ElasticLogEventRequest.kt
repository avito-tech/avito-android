package com.avito.android.elastic

import com.google.gson.annotations.SerializedName

internal class ElasticLogEventRequest(
    @SerializedName("@timestamp") val timestamp: String,
    @SerializedName("tag") val tag: String,
    @SerializedName("level") val level: String,
    @SerializedName("build_id") val buildId: String,
    @SerializedName("message") val message: String,
    @SerializedName("error_message") val errorMessage: String?
)
