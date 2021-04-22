package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.SimpleRunTest

internal interface HasFailedTestDeterminer {

    fun determine(runResult: List<SimpleRunTest>): Result

    sealed class Result {

        open fun count(): Int = 0

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
}
