package com.avito.runner.service.model

import com.avito.runner.service.worker.device.model.DeviceData
import com.avito.runner.service.worker.device.model.createStubInstance

public fun DeviceTestCaseRun.Companion.createStubInstance(
    testCaseRun: TestCaseRun = TestCaseRun.createStubInstance(),
    device: DeviceData = DeviceData.createStubInstance()
): DeviceTestCaseRun = DeviceTestCaseRun(
    testCaseRun = testCaseRun,
    device = device
)
