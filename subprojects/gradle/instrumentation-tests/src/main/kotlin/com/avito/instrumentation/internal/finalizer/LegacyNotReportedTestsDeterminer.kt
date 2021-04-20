package com.avito.instrumentation.internal.finalizer

import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage

internal class LegacyNotReportedTestsDeterminer : HasNotReportedTestsDeterminer {

    override fun determine(
        runResult: List<SimpleRunTest>,
        allTests: List<TestStaticData>
    ): HasNotReportedTestsDeterminer.Result {
        val allReportedTests = runResult.map { TestStaticDataPackage.fromSimpleRunTest(it) }

        val notReportedTests = allTests.subtract(allReportedTests)
            .map { testMetadata ->
                AndroidTest.Lost.fromTestMetadata(
                    testStaticData = testMetadata,
                    startTime = 0,
                    lastSignalTime = 0,
                    stdout = "",
                    stderr = "",
                    incident = null
                )
            }

        return if (notReportedTests.isEmpty()) {
            HasNotReportedTestsDeterminer.Result.AllTestsReported
        } else {
            HasNotReportedTestsDeterminer.Result.HasNotReportedTests(lostTests = notReportedTests)
        }
    }
}
