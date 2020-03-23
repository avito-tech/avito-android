package com.avito.instrumentation.reservation.adb

class AndroidDebugBridge(
    private val logger: (String) -> Unit = {} // TODO: use Logger interface
) {

    fun getDevice(serial: String): Device {
        return if (isRemote(serial)) {
            Device(
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

    private fun isRemote(serial: String): Boolean {
        return serial.contains(':') && serial.substringBefore(':').contains('.')
    }

}
