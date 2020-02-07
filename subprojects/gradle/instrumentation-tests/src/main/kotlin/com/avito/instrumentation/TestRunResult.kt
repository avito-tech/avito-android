package com.avito.instrumentation

import com.avito.report.model.SimpleRunTest

sealed class TestRunResult {

    data class OK(
        override val testData: List<SimpleRunTest>
    ) : TestRunResult(), HasData

    data class Suppressed(
        override val testData: List<SimpleRunTest>,
        val reason: String
    ) : TestRunResult(), HasData

    sealed class Failure : TestRunResult() {

        abstract val reason: String

        data class ThereWereFailedTests(
            override val reason: String,
            override val testData: List<SimpleRunTest>
        ) : Failure(), HasData

        data class InfrastructureFailure(
            override val reason: String
        ) : Failure()
    }
}

internal interface HasData {
    val testData: List<SimpleRunTest>
}
