package com.avito.runner.service.worker.device.model

import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.Serial

public data class DeviceData(
    public val serial: Serial,
    public val configuration: DeviceConfiguration
) {

    internal companion object
}

public fun Device.getData(): DeviceData = DeviceData(
    serial = coordinate.serial,
    configuration = DeviceConfiguration(
        api = api,
        model = model
    )
)
