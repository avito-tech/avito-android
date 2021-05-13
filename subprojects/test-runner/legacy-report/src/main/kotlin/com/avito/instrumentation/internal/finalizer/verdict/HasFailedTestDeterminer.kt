package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.SimpleRunTest

// STOPSHIP: internal and factory
public interface HasFailedTestDeterminer {

    public fun determine(runResult: List<SimpleRunTest>): Result

    public sealed class Result {

        public open fun count(): Int = 0

        public object NoFailed : Result()

        public data class Failed(
            val failed: List<SimpleRunTest>,
            val suppression: Suppression = Suppression.NoSuppressed
        ) : Result() {

            public val notSuppressed: List<SimpleRunTest> = failed.minus(suppression.tests)

            public val notSuppressedCount: Int = notSuppressed.size

            override fun count(): Int = failed.size

            public sealed class Suppression(public val tests: List<SimpleRunTest>) {

                public object NoSuppressed : Suppression(emptyList()) {
                    override fun toString(): String = "No suppressed tests"
                }

                public class SuppressedAll(tests: List<SimpleRunTest>) : Suppression(tests) {
                    override fun toString(): String = "Suppressed all by flag in build.gradle"
                }

                public class SuppressedFlaky(tests: List<SimpleRunTest>) : Suppression(tests) {
                    override fun toString(): String = "Suppressed all @Flaky tests"
                }
            }
        }
    }
}
