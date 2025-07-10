package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

public interface AdbDeviceGetAliveListener {

    public fun onGetAliveDeviceSuccess(
        device: Device,
        attempt: Int,
        durationMs: Long
    )

    public fun onGetAliveDeviceError(
        device: Device,
        attempt: Int,
        durationMs: Long
    )

    public fun onGetAliveDeviceFailed(
        device: Device,
        throwable: Throwable,
        durationMs: Long
    )
}
