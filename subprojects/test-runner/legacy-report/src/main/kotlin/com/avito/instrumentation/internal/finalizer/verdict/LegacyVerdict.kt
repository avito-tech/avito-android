package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.TestName

public sealed class LegacyVerdict {

    public abstract val message: String

    public data class Success(
        override val message: String
    ) : LegacyVerdict()

    public data class Failure(
        override val message: String,
        val prettifiedDetails: Details,
        @Transient val cause: Throwable? = null
    ) : LegacyVerdict() {

        public data class Details(
            val lostTests: Set<Test>,
            val failedTests: Set<Test>
        ) {

            public operator fun plus(other: Details): Details {
                return Details(
                    lostTests = lostTests + other.lostTests,
                    failedTests = failedTests + other.failedTests
                )
            }

            public data class Test(
                val name: TestName,
                val devices: Set<String>
            )
        }
    }
}
