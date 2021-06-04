package com.avito.android.runner.devices.internal

import com.avito.runner.service.worker.device.Serial

internal class FakeAndroidDebugBridge : AndroidDebugBridge {

    var remoteDeviceProvider: (serial: Serial.Remote) -> FakeRemoteDevice = { serial -> FakeRemoteDevice(serial) }

    override fun getRemoteDevice(serial: Serial.Remote) = remoteDeviceProvider(serial)

    override fun getLocalDevice(serial: Serial.Local) = FakeDevice(serial)
}
