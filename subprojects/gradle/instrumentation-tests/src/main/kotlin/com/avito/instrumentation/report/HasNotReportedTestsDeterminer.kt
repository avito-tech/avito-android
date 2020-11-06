package com.avito.instrumentation.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage
import org.funktionale.tries.Try

interface HasNotReportedTestsDeterminer {

    fun determine(
        runResult: Try<List<SimpleRunTest>>,
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
            runResult: Try<List<SimpleRunTest>>,
            allTests: List<TestStaticData>
        ): Result {
            return runResult.fold(
                { reportedTest ->
                    val allReportedTests = reportedTest.map { TestStaticDataPackage.fromSimpleRunTest(it) }

                    val notReportedTests = allTests.subtract(allReportedTests)
                        .map { testMetadata ->
                            AndroidTest.Lost.fromTestMetadata(
                                testStaticData = testMetadata,
                                //todo это норм разве?
                                startTime = 0,
                                lastSignalTime = 0,
                                stdout = "",
                                stderr = ""
                            )
                        }

                    if (notReportedTests.isEmpty()) {
                        Result.AllTestsReported
                    } else {
                        Result.HasNotReportedTests(lostTests = notReportedTests)
                    }
                },
                { exception -> Result.DetermineError(exception = exception) }
            )
        }
    }
}
