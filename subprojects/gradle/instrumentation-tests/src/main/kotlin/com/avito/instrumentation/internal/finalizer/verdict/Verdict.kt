package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.TestName

internal sealed class Verdict {

    abstract val message: String

    data class Success(
        override val message: String
    ) : Verdict()

    data class Failure(
        override val message: String,
        val prettifiedDetails: Details,
        @Transient val cause: Throwable? = null
    ) : Verdict() {

        data class Details(
            val lostTests: Set<Test>,
            val failedTests: Set<Test>
        ) {

            operator fun plus(other: Details): Details {
                return Details(
                    lostTests = lostTests + other.lostTests,
                    failedTests = failedTests + other.failedTests
                )
            }

            data class Test(
                val name: TestName,
                val devices: Set<String>
            )
        }
    }
}
