package com.avito.instrumentation.report

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
            val failed: List<SimpleRunTest>,
            val suppressed: Suppressed
        ) : Result() {

            override fun count(): Int = failed.size

            val notSuppressed = failed.minus(suppressed.tests)

            val notSuppressedCount = notSuppressed.size


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
                    data class SuppressedByGroups(
                        val groups: List<String>
                    ) : Reason() {
                        override fun toString(): String {
                            return "Suppressed all by grops: $groups"
                        }
                    }
                }
            }
        }
    }

    class Impl(
        private val suppressFailure: Boolean,
        private val suppressGroups: List<String>
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
                                    Result.Failed(
                                        failed = failedTests,
                                        suppressed = Result.Failed.Suppressed(
                                            tests = failedTests,
                                            reason = Result.Failed.Suppressed.Reason.SuppressedAll
                                        )
                                    )
                                }
                                else -> {
                                    val suppressed = failedTests
                                        .filter { isSuppressedByGroup(it.groupList, suppressGroups) }
                                    Result.Failed(
                                        failed = failedTests,
                                        suppressed = Result.Failed.Suppressed(
                                            tests = suppressed,
                                            reason = Result.Failed.Suppressed.Reason.SuppressedByGroups(
                                                groups = suppressGroups
                                            )
                                        )
                                    )
                                }
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

        private fun isSuppressedByGroup(groupList: List<String>, suppressGroups: List<String>): Boolean {
            return groupList.intersect(suppressGroups).isNotEmpty()
        }
    }
}
