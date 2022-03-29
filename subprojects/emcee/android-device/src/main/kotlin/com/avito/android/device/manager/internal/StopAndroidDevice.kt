package com.avito.android.device.manager.internal

import com.avito.android.device.DeviceSerial
import com.malinskiy.adam.AndroidDebugBridgeClient

internal class StopAndroidDevice(
    private val adb: AndroidDebugBridgeClient,
) {
    suspend fun execute(serial: DeviceSerial) {
        adb.execute(StopEmulatorRequest(serial))
    }
}
