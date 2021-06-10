package com.avito.runner.service.worker.device.model

import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.Serial

data class DeviceData(
    val serial: Serial,
    val configuration: DeviceConfiguration
) {

    companion object
}

fun Device.getData(): DeviceData = DeviceData(
    serial = coordinate.serial,
    configuration = DeviceConfiguration(
        api = api,
        model = model
    )
)
