package com.avito.runner.scheduler.listener

import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device

public interface TestLifecycleListener {

    public fun started(
        test: TestCase,
        device: Device,
        executionNumber: Int
    )

    public fun finished(
        result: TestResult,
        test: TestCase,
        executionNumber: Int
    )

    public companion object {

        public val STUB: TestLifecycleListener = object : TestLifecycleListener {

            override fun started(test: TestCase, device: Device, executionNumber: Int) {
            }

            override fun finished(result: TestResult, test: TestCase, executionNumber: Int) {
            }
        }
    }
}
