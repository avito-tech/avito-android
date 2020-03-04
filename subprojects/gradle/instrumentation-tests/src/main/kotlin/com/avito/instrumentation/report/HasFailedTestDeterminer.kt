package com.avito.instrumentation.report

import com.avito.report.model.Flakiness
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try

interface HasFailedTestDeterminer {

    fun determine(
        runResult: Try<List<SimpleRunTest>>
    ): Result

    sealed class Result {

        open fun count(): Int = 0

        data class DetermineError(
            val throwable: Throwable
        ) : Result()

        object NoFailed : Result()

        data class Failed(
            val failed: List<SimpleRunTest>
        ) : Result() {

            override fun count(): Int = failed.size

        }

        data class FailedWithSuppressed(
            val failed: List<SimpleRunTest>,
            val suppressed: Suppressed
        ) : Result() {

            override fun count(): Int = failed.size

            data class Suppressed(
                val tests: List<SimpleRunTest>,
                val reason: Reason
            ) {

                sealed class Reason {
                    object SuppressedAll : Reason() {
                        override fun toString(): String {
                            return "Suppressed all by flag in build.gradle"
                        }
                    }

                    object SuppressedByFlakiness : Reason() {
                        override fun toString(): String = "Suppressed all @Flaky tests"
                    }
                }
            }
        }
    }

    class Impl(
        private val suppressFailure: Boolean,
        private val suppressFlaky: Boolean
    ) : HasFailedTestDeterminer {

        override fun determine(
            runResult: Try<List<SimpleRunTest>>
        ): Result {

            return runResult.fold(
                { testData ->
                    val failedTests = testData.filter { !it.status.isSuccessful }
                    val hasFailedTests = failedTests.isNotEmpty()

                    when {
                        hasFailedTests -> {
                            when {
                                suppressFailure -> {
                                    Result.FailedWithSuppressed(
                                        failed = failedTests,
                                        suppressed = Result.FailedWithSuppressed.Suppressed(
                                            tests = failedTests,
                                            reason = Result.FailedWithSuppressed.Suppressed.Reason.SuppressedAll
                                        )
                                    )
                                }
                                suppressFlaky -> {
                                    Result.FailedWithSuppressed(
                                        failed = failedTests,
                                        suppressed = Result.FailedWithSuppressed.Suppressed(
                                            tests = failedTests.filter { it.flakiness is Flakiness.Flaky },
                                            reason = Result.FailedWithSuppressed.Suppressed.Reason.SuppressedByFlakiness
                                        )
                                    )
                                }
                                else -> Result.Failed(failed = failedTests)
                            }
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
