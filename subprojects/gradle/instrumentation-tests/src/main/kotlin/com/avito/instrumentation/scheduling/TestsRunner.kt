package com.avito.instrumentation.scheduling

import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try
import java.io.File

interface TestsRunner {

    fun runTests(
        mainApk: File?,
        testApk: File,
        runType: TestExecutor.RunType,
        reportCoordinates: ReportCoordinates,
        report: Report,
        testsToRun: List<TestWithTarget>
    ): Try<List<SimpleRunTest>>
}
