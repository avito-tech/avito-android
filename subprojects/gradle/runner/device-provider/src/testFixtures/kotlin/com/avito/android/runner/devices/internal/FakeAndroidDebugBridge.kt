package com.avito.android.runner.devices.internal

import com.avito.runner.service.worker.device.Serial

internal class FakeAndroidDebugBridge : AndroidDebugBridge {

    override fun getRemoteDevice(serial: Serial.Remote) = FakeRemoteDevice(serial)

    override fun getLocalDevice(serial: Serial.Local) = FakeDevice(serial)
}
