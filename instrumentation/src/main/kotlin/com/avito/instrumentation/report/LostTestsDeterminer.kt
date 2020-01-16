package com.avito.instrumentation.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStaticDataPackage
import org.funktionale.tries.Try

interface LostTestsDeterminer {

    fun determine(
        runResult: Try<List<SimpleRunTest>>,
        initialTestsToRun: List<TestStaticData>
    ): Result

    sealed class Result {
        object AllTestsReported : Result()
        data class ThereWereLostTests(val lostTests: List<AndroidTest.Lost>) : Result()
        data class FailedToGetLostTests(val exception: Throwable) : Result()
    }
}

class LostTestsDeterminerImplementation : LostTestsDeterminer {

    override fun determine(
        runResult: Try<List<SimpleRunTest>>,
        initialTestsToRun: List<TestStaticData>
    ): LostTestsDeterminer.Result =

        runResult.fold(
            { testData ->
                val allReportedTests = testData.map { TestStaticDataPackage.fromSimpleRunTest(it) }

                val lostTests = initialTestsToRun.subtract(allReportedTests)
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

                if (lostTests.isEmpty()) {
                    LostTestsDeterminer.Result.AllTestsReported
                } else {
                    LostTestsDeterminer.Result.ThereWereLostTests(lostTests = lostTests)
                }
            },
            { exception -> LostTestsDeterminer.Result.FailedToGetLostTests(exception = exception) }
        )
}
