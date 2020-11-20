package com.avito.plugin

import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.runner.service.worker.device.adb.AdbDeviceParams
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.commonLogger

internal interface DeviceProvider {

    fun getDevice(): AdbDevice
}

internal class DeviceProviderLocal(
    private val adb: Adb,
    private val adbDevicesManager: DevicesManager,
    private val logger: CILogger
) : DeviceProvider {

    override fun getDevice(): AdbDevice {
        val adbDeviceParams = connectedDeviceParams()

        val serial: Serial.Local = when {
            adbDeviceParams.id is Serial.Local -> adbDeviceParams.id as Serial.Local
            adbDeviceParams.id is Serial.Remote -> Serial.Local(adbDeviceParams.id.value) // TODO: support model of locally connected device
            else -> throw RuntimeException("Unsupported device id: " + adbDeviceParams.id)
        }

        return AdbDevice(
            coordinate = DeviceCoordinate.Local(serial),
            model = adbDeviceParams.model,
            online = adbDeviceParams.online,
            logger = commonLogger(logger),
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

