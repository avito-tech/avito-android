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
        object AllTestsReported : Result()
        data class HasNotReportedTests(val lostTests: List<AndroidTest.Lost>) : Result()
        data class FailedToDetermine(val exception: Throwable) : Result()
    }

    class Impl : HasNotReportedTestsDeterminer {

        override fun determine(
            runResult: Try<List<SimpleRunTest>>,
            allTests: List<TestStaticData>
        ): Result =

            runResult.fold(
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
                { exception -> Result.FailedToDetermine(exception = exception) }
            )
    }

}
