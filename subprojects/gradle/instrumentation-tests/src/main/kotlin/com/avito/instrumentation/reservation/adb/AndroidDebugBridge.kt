package com.avito.instrumentation.reservation.adb

import com.avito.runner.service.worker.device.Serial

class AndroidDebugBridge(
    private val logger: (String) -> Unit = {} // TODO: use Logger interface
) {

    fun getDevice(serial: Serial): Device {
        return if (serial is Serial.Remote) {
            RemoteDevice(
                serial = serial,
                logger = logger
            )
        } else {
            LocalDevice(
                serial = serial,
                logger = logger
            )
        }
    }
}
