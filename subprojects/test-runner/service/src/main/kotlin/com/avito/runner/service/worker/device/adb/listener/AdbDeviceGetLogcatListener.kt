package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

public interface AdbDeviceGetLogcatListener {

    public fun onLogcatSuccess(
        device: Device,
        durationMs: Long
    )

    public fun onLogcatError(
        device: Device,
        durationMs: Long,
        throwable: Throwable
    )

    public fun onLogcatFailure(
        device: Device,
        durationMs: Long,
        throwable: Throwable
    )
}
