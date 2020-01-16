package com.avito.instrumentation.report.listener

import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device
import org.funktionale.tries.Try
import java.io.File

abstract class TestReporter : TestLifecycleListener {

    override fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    ) {
    }

    override fun finished(
        artifacts: Try<File>,
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
        artifacts: Try<File>,
        test: TestCase,
        executionNumber: Int
    )
}
