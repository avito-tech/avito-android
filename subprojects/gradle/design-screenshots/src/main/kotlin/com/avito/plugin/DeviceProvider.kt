package com.avito.plugin

import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.runner.service.worker.device.adb.AdbDeviceParams

internal interface DeviceProvider {

    fun getDevice(): AdbDevice
}

internal class DeviceProviderLocal(
    private val adb: Adb,
    private val adbDevicesManager: DevicesManager,
    private val loggerFactory: LoggerFactory
) : DeviceProvider {

    override fun getDevice(): AdbDevice {
        val adbDeviceParams = connectedDeviceParams()

        val serial: Serial.Local = when (adbDeviceParams.id) {
            is Serial.Local -> adbDeviceParams.id as Serial.Local
            // TODO: support model of locally connected device
            is Serial.Remote -> Serial.Local(adbDeviceParams.id.value)
            else -> throw RuntimeException("Unsupported device id: " + adbDeviceParams.id)
        }

        return AdbDevice(
            coordinate = DeviceCoordinate.Local(serial),
            model = adbDeviceParams.model,
            online = adbDeviceParams.online,
            loggerFactory = loggerFactory,
            adb = adb
        )
    }

    private fun connectedDeviceParams(): AdbDeviceParams {
        adbDevicesManager.connectedDevices().let { set ->
            require(set.size == 1) {
                "Only single emulator is supported. Found ${set.size}"
            }
            return set.first()
        }
    }
}
