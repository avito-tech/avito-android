package com.avito.instrumentation.reservation.adb

import org.funktionale.tries.Try

class LocalDevice(
    serial: String,
    logger: (String) -> Unit = {}
) : Device(serial, logger) {

    override fun disconnect(): Try<String> {
        return Try.Success("USB device doesn't support disconnection")
    }

    override fun connect(): Try<String> {
        return Try.Success("USB device is connected already")
    }
}
