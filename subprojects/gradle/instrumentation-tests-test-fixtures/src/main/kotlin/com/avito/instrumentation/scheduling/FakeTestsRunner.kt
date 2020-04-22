package com.avito.instrumentation.scheduling

import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try
import java.io.File
import java.util.ArrayDeque
import java.util.Queue

class FakeTestsRunner(
    results: List<Try<List<SimpleRunTest>>> = emptyList()
) : TestsRunner {

    private val runTestsResultsQueue: Queue<Try<List<SimpleRunTest>>> = ArrayDeque(results)

    val runTestsRequests: MutableList<RunTestsRequest> = mutableListOf()

    @Synchronized
    override fun runTests(
        mainApk: File?,
        testApk: File,
        runType: TestExecutor.RunType,
        reportCoordinates: ReportCoordinates,
        report: Report,
        testsToRun: List<TestWithTarget>
    ): Try<List<SimpleRunTest>> {
        if (runTestsResultsQueue.isEmpty()) {
            throw IllegalArgumentException(
                "Run tests results queue is empty in FakeTestsRunner"
            )
        }

        runTestsRequests.add(
            RunTestsRequest(
                mainApk = mainApk,
                testApk = testApk,
                runType = runType,
                reportCoordinates = reportCoordinates,
                testsToRun = testsToRun
            )
        )

        return runTestsResultsQueue.poll()
    }

    data class RunTestsRequest(
        val mainApk: File?,
        val testApk: File,
        val runType: TestExecutor.RunType,
        val reportCoordinates: ReportCoordinates,
        val testsToRun: List<TestWithTarget>
    )
}
