package com.avito.instrumentation.reservation.adb

import com.avito.utils.runCommand

class AndroidDebugBridge(
    private val logger: (String) -> Unit = {}
) {

    fun getDevice(serial: String) = Device(
        serial = serial,
        logger = logger
    )

    fun printConnectedDevices() = runCommand(
        command = "adb devices",
        logger = logger
    )
}
