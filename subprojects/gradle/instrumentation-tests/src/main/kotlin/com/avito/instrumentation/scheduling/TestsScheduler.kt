package com.avito.instrumentation.scheduling

import com.avito.test.summary.FlakyInfo
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try

interface TestsScheduler {

    fun schedule(): Result

    data class Result(
        val testSuite: TestSuiteProvider.TestSuite,
        val testsResult: Try<List<SimpleRunTest>>,
        val flakyInfo: List<FlakyInfo>
    )
}
