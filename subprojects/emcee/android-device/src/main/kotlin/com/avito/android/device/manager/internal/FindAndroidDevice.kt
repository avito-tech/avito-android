package com.avito.android.device.manager.internal

import com.avito.android.device.AndroidDevice
import com.avito.android.device.DeviceSerial
import com.avito.android.device.internal.AndroidDeviceImpl
import com.avito.android.device.internal.InstallApplicationCommand
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.DeviceState
import com.malinskiy.adam.request.device.ListDevicesRequest
import com.malinskiy.adam.request.prop.GetSinglePropRequest

internal class FindAndroidDevice(
    private val adb: AndroidDebugBridgeClient,
    private val maximumRunningDevices: Int
) {

    suspend fun execute(sdk: Int, type: String): AndroidDevice? {
        val devices: List<Device> = adb.execute(request = ListDevicesRequest())
        require(devices.size <= maximumRunningDevices) {
            "More than $maximumRunningDevices device(s) are running. The previous test run may be finished incorrectly."
        }
        val foundDevice = devices.find(sdk, type)

        return if (foundDevice != null) {
            AndroidDeviceImpl(
                sdk = sdk,
                type = type,
                serial = DeviceSerial(foundDevice.serial),
                adb = adb,
                installApplicationCommand = InstallApplicationCommand(adb),
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
                val sdkPropString = adb.execute(
                    GetSinglePropRequest("ro.build.version.sdk"), device.serial
                )
                // Sometimes prop value contains `\n` at the end of the result
                val sdkProp = sdkPropString.trimIndent().toInt()
                sdkProp == sdk
            }
    }
}
