package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

public interface AdbDeviceClearPackageListener {

    public fun onClearPackageSuccess(
        device: Device,
        attempt: Int,
        name: String,
        durationMs: Long
    )

    public fun onClearPackageError(
        device: Device,
        attempt: Int,
        name: String,
        throwable: Throwable,
        durationMs: Long
    )

    public fun onClearPackageFailure(
        device: Device,
        name: String,
        throwable: Throwable,
        durationMs: Long
    )
}
