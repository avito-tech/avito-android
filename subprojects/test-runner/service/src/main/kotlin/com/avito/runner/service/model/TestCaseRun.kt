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

            sealed class InfrastructureError : Failed() {

                abstract val error: Throwable

                class Unexpected(override val error: Throwable) : InfrastructureError()

                class FailedOnStart(override val error: Throwable) : InfrastructureError()

                class FailedOnParsing(override val error: Throwable) : InfrastructureError()

                class FailOnPullingArtifacts(override val error: Throwable) : InfrastructureError()

                class Timeout(
                    val timeoutMin: Long,
                    override val error: Throwable
                ) : InfrastructureError()
            }
        }
    }

    companion object
}
