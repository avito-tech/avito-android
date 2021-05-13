package com.avito.android.runner.devices.internal

import com.avito.runner.service.worker.device.Serial

internal interface AndroidDebugBridge {
    fun getRemoteDevice(serial: Serial.Remote): RemoteDevice
    fun getLocalDevice(serial: Serial.Local): Device
}
