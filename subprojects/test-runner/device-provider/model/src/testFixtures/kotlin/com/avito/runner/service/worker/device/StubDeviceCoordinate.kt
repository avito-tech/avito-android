package com.avito.runner.service.worker.device

public fun DeviceCoordinate.Local.Companion.createStubInstance(
    serial: String = "stub"
): DeviceCoordinate.Local = DeviceCoordinate.Local(
    serial = Serial.Local(serial)
)
