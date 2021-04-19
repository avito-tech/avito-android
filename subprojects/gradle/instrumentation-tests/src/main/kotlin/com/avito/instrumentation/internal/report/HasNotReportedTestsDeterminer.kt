package com.avito.instrumentation.internal.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage

internal interface HasNotReportedTestsDeterminer {

    fun determine(
        runResult: List<SimpleRunTest>,
        allTests: List<TestStaticData>
    ): Result

    sealed class Result {

        open val lostTests
            get() = emptyList<AndroidTest.Lost>()

        object AllTestsReported : Result()

        data class HasNotReportedTests(
            override val lostTests: List<AndroidTest.Lost>
        ) : Result()

        data class DetermineError(val exception: Throwable) : Result()
    }

    class Impl : HasNotReportedTestsDeterminer {

        override fun determine(
            runResult: List<SimpleRunTest>,
            allTests: List<TestStaticData>
        ): Result {
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
                Result.AllTestsReported
            } else {
                Result.HasNotReportedTests(lostTests = notReportedTests)
            }
        }
    }
}
