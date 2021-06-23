package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

interface AdbDeviceGetAliveListener {
    fun onGetAliveDeviceSuccess(
        device: Device,
        attempt: Int,
        durationMs: Long
    )

    fun onGetAliveDeviceError(
        device: Device,
        attempt: Int,
        durationMs: Long
    )

    fun onGetAliveDeviceFailed(
        device: Device,
        throwable: Throwable,
        durationMs: Long
    )
}
