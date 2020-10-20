package com.avito.runner.service.worker.device

import java.util.Optional

interface DevicesManager {
    fun findDevice(coordinate: Serial): Optional<Device>
    fun connectedDevices(): Set<Device>
}
