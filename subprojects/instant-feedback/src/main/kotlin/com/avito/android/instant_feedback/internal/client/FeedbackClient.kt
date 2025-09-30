package com.avito.android.instant_feedback.internal.client

internal interface FeedbackClient {
    suspend fun sendRequest(data: RequestFeedbackData)
}
