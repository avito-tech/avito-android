package com.avito.runner.service.worker.device

fun DeviceCoordinate.Local.Companion.createStubInstance(
    serial: Serial.Local = Serial.Local("stub")
): DeviceCoordinate.Local = DeviceCoordinate.Local(
    serial = serial
)
