package com.avito.instrumentation.internal.report.listener

import com.avito.android.runner.report.Report
import com.avito.report.model.AndroidTest
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.listener.TestResult
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device
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
        logcatBuffers.create(key, LogcatBuffer.Impl(logcatFile = logcatFile))
    }

    override fun finished(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    ) {
        val key = LogcatBuffers.Key(test, executionNumber)

        @Suppress("MoveVariableDeclarationIntoWhen")
        val testReport = reportProcessor.createTestReport(
            result = result,
            test = test,
            executionNumber = executionNumber,
            logcatBuffer = logcatBuffers.get(key)
        )

        when (testReport) {
            is AndroidTest.Completed -> report.sendCompletedTest(testReport)
            is AndroidTest.Lost -> report.sendLostTests(listOf(testReport))
            is AndroidTest.Skipped -> {
                /* do nothing */
            }
        }

        logcatBuffers.destroy(key)
    }
}
