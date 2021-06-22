package com.avito.runner.finalizer.verdict

import com.avito.test.model.TestName

internal sealed class LegacyVerdict {

    abstract val message: String

    data class Success(
        override val message: String
    ) : LegacyVerdict()

    data class Failure(
        override val message: String,
        val prettifiedDetails: Details,
        @Transient val cause: Throwable? = null
    ) : LegacyVerdict() {

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
