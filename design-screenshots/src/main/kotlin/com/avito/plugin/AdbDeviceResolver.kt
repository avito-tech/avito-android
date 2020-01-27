package com.avito.plugin

import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.utils.logging.CILogger
import org.funktionale.either.Either

interface AdbDeviceResolver {
    fun resolve(eitherDevice: Either<AdbDevice, Exception>): AdbDevice
}

internal class AdbDeviceResolverLocal(val ciLogger: CILogger) : AdbDeviceResolver {
    override fun resolve(eitherDevice: Either<AdbDevice, Exception>): AdbDevice {
        if (eitherDevice.isLeft()) {
            ciLogger.info("One device found, gonna start pulling")
            return eitherDevice.left().get()
        } else {
            val exception = eitherDevice.right().get()
            ciLogger.critical(exception.message.toString(), exception)
            throw exception
        }
    }
}