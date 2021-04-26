package com.avito.instrumentation.internal.scheduling

import com.avito.instrumentation.internal.suite.TestSuiteProvider

internal interface TestsScheduler {

    fun schedule(): Result

    data class Result(val testSuite: TestSuiteProvider.TestSuite)
}
