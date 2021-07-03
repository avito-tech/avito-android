package com.avito.runner.service.worker.device

import com.avito.runner.service.worker.device.adb.AdbDeviceParams
import java.util.Optional

public interface DevicesManager {

    public fun findDevice(coordinate: Serial): Optional<AdbDeviceParams>

    public fun connectedDevices(): Set<AdbDeviceParams>
}
