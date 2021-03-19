package com.avito.instrumentation.internal.report

import com.avito.report.model.Flakiness
import com.avito.report.model.SimpleRunTest

internal interface HasFailedTestDeterminer {

    fun determine(
        runResult: com.avito.android.Result<List<SimpleRunTest>>
    ): Result

    sealed class Result {

        open fun count(): Int = 0

        data class DetermineError(
            val throwable: Throwable
        ) : Result()

        object NoFailed : Result()

        data class Failed(
            val failed: List<SimpleRunTest>,
            val suppression: Suppression = Suppression.NoSuppressed
        ) : Result() {

            val notSuppressed = failed.minus(suppression.tests)

            val notSuppressedCount = notSuppressed.size

            override fun count(): Int = failed.size

            sealed class Suppression(val tests: List<SimpleRunTest>) {

                object NoSuppressed : Suppression(emptyList()) {
                    override fun toString(): String = "No suppressed tests"
                }

                class SuppressedAll(tests: List<SimpleRunTest>) : Suppression(tests) {
                    override fun toString(): String = "Suppressed all by flag in build.gradle"
                }

                class SuppressedFlaky(tests: List<SimpleRunTest>) : Suppression(tests) {
                    override fun toString(): String = "Suppressed all @Flaky tests"
                }
            }
        }
    }

    class Impl(
        private val suppressFailure: Boolean,
        private val suppressFlaky: Boolean
    ) : HasFailedTestDeterminer {

        override fun determine(
            runResult: com.avito.android.Result<List<SimpleRunTest>>
        ): Result {

            return runResult.fold(
                { testData ->
                    val failedTests = testData.filter { !it.status.isSuccessful }
                    val hasFailedTests = failedTests.isNotEmpty()

                    when {
                        hasFailedTests ->
                            when {
                                suppressFailure -> {
                                    Result.Failed(
                                        failed = failedTests,
                                        suppression = Result.Failed.Suppression.SuppressedAll(
                                            tests = failedTests
                                        )
                                    )
                                }
                                suppressFlaky -> {
                                    Result.Failed(
                                        failed = failedTests,
                                        suppression = Result.Failed.Suppression.SuppressedFlaky(
                                            tests = failedTests.filter { it.flakiness is Flakiness.Flaky }
                                        )
                                    )
                                }
                                else -> Result.Failed(failed = failedTests)
                            }
                        else -> Result.NoFailed
                    }
                },
                { exception ->
                    Result.DetermineError(exception)
                }
            )
        }
    }
}
