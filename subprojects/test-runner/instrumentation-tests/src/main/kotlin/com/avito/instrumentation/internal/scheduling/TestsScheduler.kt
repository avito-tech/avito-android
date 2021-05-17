package com.avito.instrumentation.internal.scheduling

import com.avito.instrumentation.internal.suite.TestSuiteProvider
import com.avito.report.model.AndroidTest

internal interface TestsScheduler {

    fun schedule(): Result

    data class Result(
        val testSuite: TestSuiteProvider.TestSuite,
        val testResults: Collection<AndroidTest>
    )
}
