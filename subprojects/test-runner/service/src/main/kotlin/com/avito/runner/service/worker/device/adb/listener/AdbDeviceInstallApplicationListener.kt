package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

public interface AdbDeviceInstallApplicationListener {

    public fun onInstallApplicationSuccess(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        durationMs: Long
    )

    public fun onInstallApplicationError(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    )

    public fun onInstallApplicationFailure(
        device: Device,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    )
}
