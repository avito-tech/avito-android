package com.avito.runner.report

import com.avito.report.model.AndroidTest
import com.avito.runner.logcat.LogcatAccessor
import com.avito.runner.model.TestResult
import com.avito.test.model.TestCase

/**
 * todo remove in favour of TestArtifactsProcessorImpl after uploadAllArtifacts flag removed
 */
internal interface ReportProcessor {

    fun createTestReport(
        result: TestResult,
        test: TestCase,
        executionNumber: Int,
        logcatAccessor: LogcatAccessor
    ): AndroidTest
}
