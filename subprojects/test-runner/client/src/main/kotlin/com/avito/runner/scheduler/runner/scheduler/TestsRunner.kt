package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.runner.report.Report
import com.avito.runner.scheduler.runner.model.TestWithTarget
import java.io.File

internal interface TestsRunner {

    fun runTests(
        mainApk: File?,
        testApk: File,
        report: Report,
        testsToRun: List<TestWithTarget>
    )
}
