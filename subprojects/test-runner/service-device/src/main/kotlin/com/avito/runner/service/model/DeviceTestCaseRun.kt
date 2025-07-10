package com.avito.runner.service.model

import com.avito.runner.model.TestCaseRun
import com.avito.runner.service.worker.device.model.DeviceData

public data class DeviceTestCaseRun(
    val testCaseRun: TestCaseRun,
    val device: DeviceData
) {

    public companion object
}
