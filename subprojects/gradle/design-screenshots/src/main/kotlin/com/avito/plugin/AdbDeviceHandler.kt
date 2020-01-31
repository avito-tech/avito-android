package com.avito.plugin

import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.utils.logging.CILogger
import org.funktionale.either.Either

interface AdbDeviceHandler {
    fun resolve(eitherDevice: Either<Exception, AdbDevice>): AdbDevice
}

internal class AdbDeviceHandlerLocal(val ciLogger: CILogger) : AdbDeviceHandler {
    override fun resolve(eitherDevice: Either<Exception, AdbDevice>): AdbDevice {
        if (eitherDevice.isRight()) {
            ciLogger.info("One device found, gonna start pulling")
            return eitherDevice.right().get()
        } else {
            val exception = eitherDevice.left().get()
            ciLogger.critical(exception.message.toString(), exception)
            throw exception
        }
    }
}