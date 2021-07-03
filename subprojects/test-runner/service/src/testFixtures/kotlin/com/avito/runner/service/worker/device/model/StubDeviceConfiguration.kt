package com.avito.runner.service.worker.device.model

public fun DeviceConfiguration.Companion.createStubInstance(
    api: Int = 29,
    model: String = "model"
): DeviceConfiguration = DeviceConfiguration(
    api = api,
    model = model
)
