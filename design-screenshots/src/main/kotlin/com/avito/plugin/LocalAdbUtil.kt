package com.avito.plugin

import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.adb.AdbDevice
import org.funktionale.either.Either

internal fun getDevice(adbDevicesManager: DevicesManager, adbDeviceResolver: AdbDeviceResolver): AdbDevice {
    adbDevicesManager.connectedDevices().let { set ->
        val either = when (set.size) {
            0 -> {
                val exception = Exception("There are no connected devices")
                Either.right(exception)
            }
            1 -> {
                Either.left(set.first() as AdbDevice)
            }
            else -> {
                val exception =
                    Exception("There are too much devices, turn them off until there will be one device only")
                Either.right(exception)
            }
        }
        return adbDeviceResolver.resolve(either)
    }
}