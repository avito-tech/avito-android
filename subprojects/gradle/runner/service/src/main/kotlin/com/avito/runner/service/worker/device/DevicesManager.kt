package com.avito.runner.service.worker.device

interface DevicesManager {
    fun connectedDevices(): Set<Device>
}
