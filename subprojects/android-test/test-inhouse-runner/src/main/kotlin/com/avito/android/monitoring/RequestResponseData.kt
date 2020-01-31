package com.avito.android.monitoring

data class RequestResponseData(
    val requestUrl: String,
    val host: String,
    val requestBody: String?,
    val responseCode: Int,
    val responseBody: String?
)
