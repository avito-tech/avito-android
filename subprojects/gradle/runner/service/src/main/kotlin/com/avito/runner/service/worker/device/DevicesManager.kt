package com.avito.runner.service.worker.device

import com.avito.runner.service.worker.device.adb.AdbDeviceParams
import java.util.Optional

interface DevicesManager {
    fun findDevice(coordinate: Serial): Optional<AdbDeviceParams>
    fun connectedDevices(): Set<AdbDeviceParams>
}
