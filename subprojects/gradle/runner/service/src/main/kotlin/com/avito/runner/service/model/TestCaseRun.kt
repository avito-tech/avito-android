package com.avito.runner.service.model

data class TestCaseRun(
    val test: TestCase,
    val result: Result,
    val timestampStartedMilliseconds: Long,
    val timestampCompletedMilliseconds: Long
) {
    val durationMilliseconds: Long
        get() = timestampCompletedMilliseconds - timestampStartedMilliseconds

    sealed class Result {
        object Passed : Result()
        object Ignored : Result()
        sealed class Failed : Result() {
            data class InRun(val errorMessage: String) : Failed()

            data class InfrastructureError(
                val errorMessage: String,
                val cause: Throwable? = null
            ) : Failed()
        }
    }
}
