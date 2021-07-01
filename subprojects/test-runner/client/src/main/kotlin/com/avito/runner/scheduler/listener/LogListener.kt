package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import com.avito.test.model.TestCase
import com.avito.time.millisecondsToHumanReadableTime
import java.io.File

internal class LogListener : TestListener {

    override fun started(
        device: Device,
        targetPackage: String,
        test: TestCase,
        executionNumber: Int
    ) {
        device.logger.info("Test started: $test")
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
        val status = when (result) {
            is TestCaseRun.Result.Passed -> "PASSED"
            is TestCaseRun.Result.Ignored -> "IGNORED"
            is TestCaseRun.Result.Failed.InRun -> "FAILED"
            is TestCaseRun.Result.Failed.InfrastructureError -> {
                device.logger.warn("$test LOST", result.error)
                "LOST"
            }
        }
        device.logger.info(
            "Test $status in ${durationMilliseconds.millisecondsToHumanReadableTime()}: $test"
        )
    }
}
