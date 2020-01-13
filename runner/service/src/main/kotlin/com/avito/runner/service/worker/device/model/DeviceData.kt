package com.avito.runner.service.worker.device.model

import com.avito.runner.service.worker.device.Device

data class DeviceData(
    val serial: String,
    val configuration: DeviceConfiguration
)

fun Device.getData(): DeviceData = DeviceData(
    serial = id,
    configuration = DeviceConfiguration(
        api = api
    )
)
