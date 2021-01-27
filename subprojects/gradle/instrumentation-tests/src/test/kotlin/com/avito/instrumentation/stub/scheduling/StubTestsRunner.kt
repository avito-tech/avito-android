package com.avito.instrumentation.stub.scheduling

import com.avito.instrumentation.internal.executing.TestExecutor
import com.avito.instrumentation.internal.scheduling.TestsRunner
import com.avito.instrumentation.internal.suite.model.TestWithTarget
import com.avito.instrumentation.report.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try
import java.io.File
import java.util.ArrayDeque
import java.util.Queue

internal class StubTestsRunner(
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
                "Run tests results queue is empty in StubTestsRunner"
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
