package com.avito.android.instant_feedback.internal.client

internal class FakeFeedbackClient : FeedbackClient {

    private val captured: MutableList<RequestFeedbackData> = mutableListOf()
    private var shouldFail: Boolean = false
    private var failureToThrow: Throwable = RuntimeException("Simulated failure")

    val requests: List<RequestFeedbackData> = captured

    override suspend fun sendRequest(data: RequestFeedbackData) {
        if (shouldFail) {
            throw failureToThrow
        }
        captured.add(data)
    }

    fun simulateFailure() {
        shouldFail = true
    }
}
