package com.avito.instrumentation.internal.scheduling

import com.avito.instrumentation.internal.executing.TestExecutor
import com.avito.instrumentation.internal.suite.model.TestWithTarget
import com.avito.instrumentation.report.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try
import java.io.File

internal interface TestsRunner {

    fun runTests(
        mainApk: File?,
        testApk: File,
        runType: TestExecutor.RunType,
        reportCoordinates: ReportCoordinates,
        report: Report,
        testsToRun: List<TestWithTarget>
    ): Try<List<SimpleRunTest>>
}
