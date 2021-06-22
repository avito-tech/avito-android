package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

interface AdbDeviceGetLogcatListener {
    fun onLogcatSuccess(
        device: Device,
        durationMs: Long
    )

    fun onLogcatError(
        device: Device,
        durationMs: Long,
        throwable: Throwable
    )

    fun onLogcatFailure(
        device: Device,
        durationMs: Long,
        throwable: Throwable
    )
}
