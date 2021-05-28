package com.avito.android.runner.devices.internal

import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.ProcessRunner

internal class AndroidDebugBridgeImpl(
    private val adb: Adb,
    private val loggerFactory: LoggerFactory
) : AndroidDebugBridge {

    private val processRunner = ProcessRunner.Real(null)

    override fun getRemoteDevice(serial: Serial.Remote): RemoteDeviceImpl {
        return RemoteDeviceImpl(
            serial = serial,
            adb = adb,
            processRunner = processRunner,
            loggerFactory = loggerFactory
        )
    }

    override fun getLocalDevice(serial: Serial.Local): LocalDevice {
        return LocalDevice(
            serial = serial,
            adb = adb,
            processRunner = processRunner,
            loggerFactory = loggerFactory
        )
    }
}
