package com.avito.android.elastic

import com.google.gson.annotations.SerializedName

@Suppress("unused")
internal class ElasticLogEventRequest(
    @SerializedName("@timestamp") val timestamp: String,
    val tag: String,
    val level: String,
    val buildId: String,
    val message: String,
    val errorMessage: String?
)
