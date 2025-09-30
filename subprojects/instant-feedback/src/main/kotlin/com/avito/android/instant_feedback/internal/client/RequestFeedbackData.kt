package com.avito.android.instant_feedback.internal.client

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Serializable
public data class RequestFeedbackData(
    val scenario: String,
    val user: String,
    val arguments: Map<String, String> = emptyMap(),
    val date: String = currentDate()
)

private fun currentDate(): String {
    val instant = Instant.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    return instant.atOffset(ZoneOffset.UTC).format(formatter)
}
