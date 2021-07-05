package com.avito.runner.scheduler.listener

import com.avito.report.Report
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestAttempt
import com.avito.runner.scheduler.logcat.BufferLogcatAccessor
import com.avito.runner.scheduler.logcat.LogcatBuffers
import com.avito.runner.scheduler.logcat.ResultLogcatAccessor
import com.avito.runner.scheduler.logcat.TailingLogcatBuffer
import com.avito.runner.service.worker.device.Device
import com.avito.test.model.TestCase
import java.io.File

internal class LogcatTestLifecycleListener(
    private val logcatDir: File,
    private val reportProcessor: ReportProcessor,
    private val report: Report
) : TestLifecycleListener {

    private val logcatBuffers = LogcatBuffers()

    override fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    ) {
        val logcatFile = File(logcatDir, "${device.coordinate.serial}.txt")

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
