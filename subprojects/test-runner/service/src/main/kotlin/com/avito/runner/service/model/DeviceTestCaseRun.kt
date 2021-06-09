package com.avito.runner.service.model

import com.avito.runner.service.worker.device.model.DeviceData

data class DeviceTestCaseRun(
    val testCaseRun: TestCaseRun,
    val device: DeviceData
) {

    companion object
}
