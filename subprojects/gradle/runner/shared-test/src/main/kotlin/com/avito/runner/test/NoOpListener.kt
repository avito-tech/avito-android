package com.avito.runner.test

import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device

object NoOpListener : TestListener {
    override fun started(device: Device, targetPackage: String, test: TestCase, executionNumber: Int) {
        // empty
    }

    override fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int
    ) {
        // empty
    }
}
