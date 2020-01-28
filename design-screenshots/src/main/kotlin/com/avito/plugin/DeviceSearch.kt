package com.avito.plugin

import com.android.sdklib.devices.DeviceManager
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.adb.AdbDevice
import org.funktionale.either.Either

interface DeviceSearch {
    fun getDevice(): Either<Exception, AdbDevice>
}

    class DeviceSearchLocal(
        private val adbDevicesManager: DevicesManager
    ) : DeviceSearch {
        override fun getDevice(): Either<Exception, AdbDevice> {
            adbDevicesManager.connectedDevices().let { set ->
                val either = when (set.size) {
                    0 -> {
                        val exception = Exception("There are no connected devices")
                        Either.left(exception)
                    }
                    1 -> {
                        Either.right(set.first() as AdbDevice)
                    }
                    else -> {
                        val exception =
                            Exception("There are too much devices, turn them off until there will be one device only")
                        Either.left(exception)
                    }
                }
                return either
            }
        }
    }

