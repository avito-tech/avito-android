package com.avito.runner.service.worker.device.observer

import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.channels.ReceiveChannel

interface DevicesObserver {
    suspend fun observeDevices(): ReceiveChannel<Device>
}
