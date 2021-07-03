package com.avito.runner.service.worker.model

import com.avito.runner.service.worker.device.model.DeviceData

public data class DeviceInstallation(
    public val installation: Installation,
    public val device: DeviceData
)
