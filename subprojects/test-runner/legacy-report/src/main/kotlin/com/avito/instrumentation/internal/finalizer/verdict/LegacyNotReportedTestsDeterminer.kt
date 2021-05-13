package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.fromSimpleRunTest
import com.avito.time.TimeProvider

// STOPSHIP: internal and factory
public class LegacyNotReportedTestsDeterminer(
    private val timeProvider: TimeProvider
) : HasNotReportedTestsDeterminer {

    override fun determine(
        runResult: List<SimpleRunTest>,
        allTests: List<TestStaticData>
    ): HasNotReportedTestsDeterminer.Result {
        val allReportedTests = runResult.map { TestStaticDataPackage.fromSimpleRunTest(it) }

        val notReportedTests = allTests.subtract(allReportedTests)
            .map { testMetadata ->
                AndroidTest.Lost.createWithoutInfo(
                    testStaticData = testMetadata,
                    currentTimeSec = timeProvider.nowInSeconds()
                )
            }

        return if (notReportedTests.isEmpty()) {
            HasNotReportedTestsDeterminer.Result.AllTestsReported
        } else {
            HasNotReportedTestsDeterminer.Result.HasNotReportedTests(lostTests = notReportedTests)
        }
    }
}
