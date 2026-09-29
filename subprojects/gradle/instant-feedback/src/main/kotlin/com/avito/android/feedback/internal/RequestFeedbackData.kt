package com.avito.android.feedback.internal

import kotlinx.serialization.Serializable

@Serializable
internal data class RequestFeedbackData(
    val scenario: String,
    val user: String,
    val arguments: Map<String, String>,
    val date: String,
)
