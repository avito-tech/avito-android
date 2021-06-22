package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

interface AdbDeviceInstallApplicationListener {
    fun onInstallApplicationSuccess(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        durationMs: Long
    )

    fun onInstallApplicationError(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    )

    fun onInstallApplicationFailure(
        device: Device,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    )
}
