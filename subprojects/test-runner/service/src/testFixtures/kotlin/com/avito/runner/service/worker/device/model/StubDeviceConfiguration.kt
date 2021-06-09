package com.avito.runner.service.worker.device.model

fun DeviceConfiguration.Companion.createStubInstance(
    api: Int = 29,
    model: String = "TestAndroid"
): DeviceConfiguration = DeviceConfiguration(
    api = api,
    model = model
)
