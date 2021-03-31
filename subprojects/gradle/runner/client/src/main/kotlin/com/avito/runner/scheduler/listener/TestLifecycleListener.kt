package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun.Result.Failed.InfrastructureError
import com.avito.runner.service.worker.device.Device
import java.io.File

interface TestLifecycleListener {

    fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    )

    fun finished(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    )

    sealed class TestResult {
        class Complete(val artifacts: Result<File>) : TestResult()
        class Incomplete(val infraError: InfrastructureError) : TestResult()
    }
}
