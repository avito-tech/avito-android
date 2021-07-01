package com.avito.runner.scheduler.listener

import com.avito.report.model.AndroidTest
import com.avito.runner.scheduler.logcat.LogcatAccessor
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
