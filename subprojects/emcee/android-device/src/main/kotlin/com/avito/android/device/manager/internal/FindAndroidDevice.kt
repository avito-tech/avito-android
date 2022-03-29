package com.avito.android.device.manager.internal

import com.avito.android.device.AndroidDevice
import com.avito.android.device.DeviceSerial
import com.avito.android.device.internal.AndroidDeviceImpl
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.DeviceState
import com.malinskiy.adam.request.device.ListDevicesRequest
import com.malinskiy.adam.request.prop.GetSinglePropRequest

internal class FindAndroidDevice(
    private val adb: AndroidDebugBridgeClient,
    private val stop: StopAndroidDevice,
    private val maximumRunningDevices: Int
) {

    suspend fun execute(sdk: Int, type: String): AndroidDevice? {
        val devices: List<Device> = adb.execute(request = ListDevicesRequest())
        require(devices.size <= maximumRunningDevices) {
            "Must be maximum $maximumRunningDevices running devices per worker"
        }
        val foundDevice = devices.find(sdk, type)
        devices
            .filter { it.serial != foundDevice?.serial }
            .forEach { device -> stop.execute(DeviceSerial(device.serial)) }

        return if (foundDevice != null) {
            AndroidDeviceImpl(
                sdk = sdk,
                type = type,
                serial = DeviceSerial(foundDevice.serial),
                adb = adb
            )
        } else {
            null
        }
    }

    private suspend fun List<Device>.find(
        sdk: Int,
        @Suppress("UNUSED_PARAMETER")
        type: String,
    ): Device? {
        return this
            .filter { it.state == DeviceState.DEVICE }
            .firstOrNull { device ->
                val sdkProp = adb.execute(
                    GetSinglePropRequest("ro.build.version.sdk"), device.serial
                ).toInt()
                sdkProp == sdk
            }
    }
}
