package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device
import java.io.File

internal abstract class TestReporter : TestLifecycleListener {

    override fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    ) {
    }

    override fun finished(
        artifacts: Result<File>,
        test: TestCase,
        executionNumber: Int
    ) {
        report(
            artifacts = artifacts,
            test = test,
            executionNumber = executionNumber
        )
    }

    protected abstract fun report(
        artifacts: Result<File>,
        test: TestCase,
        executionNumber: Int
    )
}
