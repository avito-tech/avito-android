package com.avito.runner.scheduler.listener

import com.avito.runner.millisecondsToHumanReadableTime
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device

class LogListener : TestListener {

    override fun started(
        device: Device,
        targetPackage: String,
        test: TestCase,
        executionNumber: Int
    ) {
        device.log("Test started: ${test.className}.${test.methodName}")
    }

    override fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int
    ) {
        val status = when (result) {
            is TestCaseRun.Result.Passed -> "passed"
            is TestCaseRun.Result.Ignored -> "ignored"
            is TestCaseRun.Result.Failed -> "failed"
        }

        device.log(
            "Test $status in ${durationMilliseconds.millisecondsToHumanReadableTime()}: " +
                "${test.className}.${test.methodName}"
        )
        if (result is TestCaseRun.Result.Failed) {
            device.log(result.stacktrace)
        }
    }
}
