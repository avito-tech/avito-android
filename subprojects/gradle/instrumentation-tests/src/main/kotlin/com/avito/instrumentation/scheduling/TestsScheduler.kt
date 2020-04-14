package com.avito.instrumentation.scheduling

import com.avito.instrumentation.report.FlakyInfo
import com.avito.instrumentation.rerun.BuildOnTargetCommitForTest
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try

interface TestsScheduler {

    fun schedule(
        buildOnTargetCommitResult: BuildOnTargetCommitForTest.Result
    ): Result

    data class Result(
        val initialTestSuite: TestSuiteProvider.TestSuite,
        val initialTestsResult: Try<List<SimpleRunTest>>,
        val testResultsAfterBranchReruns: Try<List<SimpleRunTest>>,
        val flakyInfo: List<FlakyInfo>
    )
}
