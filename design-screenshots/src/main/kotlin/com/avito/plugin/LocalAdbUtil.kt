package com.avito.plugin

import com.avito.runner.logging.StdOutLogger
import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.logging.CILogger

internal fun getCurrentDevice(ciLogger: CILogger): AdbDevice? {
    val adbDevicesManager = AdbDevicesManager(StdOutLogger())
    adbDevicesManager.connectedDevices().let { set ->
        return when (set.size) {
            0 -> {
                val exception = Exception("There are no connected devices")
                ciLogger.critical("There are no connected devices", exception)
                throw exception
            }
            1 -> {
                ciLogger.info("One device found, gonna start pulling")
                set.first() as? AdbDevice
            }
            else -> {
                val exception =
                    Exception("There are too much devices, turn them off until there will be one device only")
                ciLogger.critical(
                    "There are too much devices, turn them off until there will be one device only",
                    exception
                )
                throw exception
            }
        }
    }
}