package com.avito.runner.listener

import com.avito.android.Result
import com.avito.runner.model.TestCaseRun
import com.avito.runner.scheduler.metrics.InstrumentationMetricsSender
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.worker.device.Device
import com.avito.test.model.TestCase
import java.io.File

internal class TestMetricsListener(
    private val metricsSender: InstrumentationMetricsSender
) : TestListener {

    override fun started(device: Device, targetPackage: String, test: TestCase, executionNumber: Int) {
        // do nothing
    }

    override fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int,
        testArtifactsDir: Result<File>
    ) {
        when (result) {
            is TestCaseRun.Result.Failed.InRun -> {
            }

            is TestCaseRun.Result.Failed.InfrastructureError.FailOnPullingArtifacts ->
                metricsSender.sendReportFileNotAvailable()

            is TestCaseRun.Result.Failed.InfrastructureError.FailedOnParsing ->
                metricsSender.sendFailedOnParsingInstrumentation()

            is TestCaseRun.Result.Failed.InfrastructureError.FailedOnStart ->
                metricsSender.sendFailedOnStartInstrumentation()

            is TestCaseRun.Result.Failed.InfrastructureError.Timeout ->
                metricsSender.sendTimeOut()

            is TestCaseRun.Result.Failed.InfrastructureError.Unexpected ->
                metricsSender.sendUnexpectedInfraError()

            TestCaseRun.Result.Ignored -> {
            }

            TestCaseRun.Result.Passed -> {
            }
        }
    }
}
