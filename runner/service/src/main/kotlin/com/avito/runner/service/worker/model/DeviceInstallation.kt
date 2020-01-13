package com.avito.runner.service.worker.model

import com.avito.runner.service.worker.device.model.DeviceData

data class DeviceInstallation(
    val installation: Installation,
    val device: DeviceData
)
