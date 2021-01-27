package com.avito.instrumentation.internal.reservation.adb

import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.ProcessRunner

class AndroidDebugBridge(
    private val adb: Adb,
    private val loggerFactory: LoggerFactory
) {

    private val processRunner = ProcessRunner.Real(null, loggerFactory)

    fun getRemoteDevice(serial: Serial.Remote): RemoteDevice {
        return RemoteDevice(
            serial = serial,
            adb = adb,
            processRunner = processRunner,
            loggerFactory = loggerFactory
        )
    }

    fun getLocalDevice(serial: Serial.Local): LocalDevice {
        return LocalDevice(
            serial = serial,
            adb = adb,
            loggerFactory = loggerFactory
        )
    }
}
