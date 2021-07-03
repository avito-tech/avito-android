package com.avito.runner.service.worker.device.model

import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.createStubInstance

internal fun DeviceData.Companion.createStubInstance(
    serial: Serial = DeviceCoordinate.Local.createStubInstance().serial,
    configuration: DeviceConfiguration = DeviceConfiguration.createStubInstance()
): DeviceData = DeviceData(
    serial = serial,
    configuration = configuration
)
