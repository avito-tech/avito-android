package com.avito.runner.service.worker.device.model

import com.avito.runner.service.worker.device.Serial

internal fun DeviceData.Companion.createStubInstance(
    serial: Serial = Serial.Local("stub"),
    configuration: DeviceConfiguration = DeviceConfiguration.createStubInstance()
): DeviceData = DeviceData(
    serial = serial,
    configuration = configuration
)
