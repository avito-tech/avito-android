package com.avito.runner.scheduler.metrics.model

internal fun DeviceKey.Companion.createStubInstance(serial: String = "12345") = DeviceKey(serial)

internal fun String.toDeviceKey() = DeviceKey.createStubInstance(this)
