package com.avito.plugin

import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.runner.service.worker.device.adb.AdbDeviceFactory
import com.avito.runner.service.worker.device.adb.AdbDeviceParams

internal interface DeviceProvider {

    fun getDevice(): AdbDevice
}

internal class DeviceProviderLocal(
    private val adbDevicesManager: DevicesManager,
    private val adbDeviceFactory: AdbDeviceFactory
) : DeviceProvider {

    override fun getDevice(): AdbDevice {
        val adbDeviceParams = connectedDeviceParams()

        val serial: Serial.Local = when (adbDeviceParams.id) {
            is Serial.Local -> adbDeviceParams.id as Serial.Local
            // TODO: support model of locally connected device
            is Serial.Remote -> Serial.Local(adbDeviceParams.id.value)
            else -> throw RuntimeException("Unsupported device id: " + adbDeviceParams.id)
        }

        return adbDeviceFactory.create(
            coordinate = DeviceCoordinate.Local(serial),
            adbDeviceParams
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
