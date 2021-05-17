package com.avito.instrumentation.internal.scheduling

import com.avito.android.runner.report.Report
import com.avito.instrumentation.internal.suite.model.TestWithTarget
import java.io.File

internal interface TestsRunner {

    fun runTests(
        mainApk: File?,
        testApk: File,
        report: Report,
        testsToRun: List<TestWithTarget>
    )
}
