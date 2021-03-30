package com.avito.instrumentation.internal.report.listener

import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.listener.TestLifecycleListener.TestResult
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device

internal abstract class TestReporter : TestLifecycleListener {

    override fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    ) {
    }

    override fun finished(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    ) {
        report(
            result = result,
            test = test,
            executionNumber = executionNumber
        )
    }

    protected abstract fun report(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    )
}
