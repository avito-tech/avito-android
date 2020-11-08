package com.avito.instrumentation.reservation.adb

import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb

class AndroidDebugBridge(
    private val adb: Adb,
    private val logger: (String) -> Unit = {} // TODO: use Logger interface
) {
    fun getRemoteDevice(serial: Serial.Remote): RemoteDevice {
        return RemoteDevice(
            serial = serial,
            adb = adb,
            logger = logger
        )
    }

    fun getLocalDevice(serial: Serial.Local): LocalDevice {
        return LocalDevice(
            serial = serial,
            adb = adb,
            logger = logger
        )
    }
}
