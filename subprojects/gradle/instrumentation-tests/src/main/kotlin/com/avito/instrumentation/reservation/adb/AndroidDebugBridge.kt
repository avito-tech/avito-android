package com.avito.instrumentation.reservation.adb

import com.google.common.net.InetAddresses

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
        return serial.contains(':')
            && InetAddresses.isInetAddress(serial.substringBefore(':'))
    }

}
