package com.avito.runner.listener

import com.avito.report.Report
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestAttempt
import com.avito.runner.logcat.BufferLogcatAccessor
import com.avito.runner.logcat.LogcatBuffers
import com.avito.runner.logcat.ResultLogcatAccessor
import com.avito.runner.logcat.TailingLogcatBuffer
import com.avito.runner.model.TestResult
import com.avito.runner.report.ReportProcessor
import com.avito.test.model.TestCase
import java.io.File

internal class ReportTestListener(
    private val logcatDir: File,
    private val reportProcessor: ReportProcessor,
    private val report: Report
) : TestLifecycleListener {

    private val logcatBuffers = LogcatBuffers()

    override fun started(
        test: TestCase,
        deviceId: String,
        executionNumber: Int
    ) {
        val logcatFile = File(logcatDir, "$deviceId.txt")

        val key = LogcatBuffers.Key(test, executionNumber)
        logcatBuffers.create(key, TailingLogcatBuffer(logcatFile = logcatFile))
    }

    override fun finished(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    ) {
        val key = LogcatBuffers.Key(test, executionNumber)

        val testReport = reportProcessor.createTestReport(
            result = result,
            test = test,
            executionNumber = executionNumber,
            logcatAccessor = when (result) {
                is TestResult.Complete -> BufferLogcatAccessor(logcatBuffers.get(key))
                is TestResult.Incomplete -> ResultLogcatAccessor(result.logcat)
            }
        )

        when (testReport) {
            is AndroidTest.Completed,
            is AndroidTest.Lost -> report.addTest(TestAttempt(testReport, executionNumber))

            is AndroidTest.Skipped -> {
                /* do nothing, all skipped tests already sent via Report.addSkippedTests() */
            }
        }

        logcatBuffers.destroy(key)
    }
}
