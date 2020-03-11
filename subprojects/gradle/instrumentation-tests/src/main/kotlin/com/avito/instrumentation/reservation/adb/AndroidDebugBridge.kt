package com.avito.instrumentation.reservation.adb

class AndroidDebugBridge(
    private val logger: (String) -> Unit = {}
) {

    fun getDevice(serial: String) = Device(
        serial = serial,
        logger = logger
    )
}
