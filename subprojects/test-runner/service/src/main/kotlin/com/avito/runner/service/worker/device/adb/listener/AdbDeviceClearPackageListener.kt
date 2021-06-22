package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

interface AdbDeviceClearPackageListener {

    fun onClearPackageSuccess(
        device: Device,
        attempt: Int,
        name: String,
        durationMs: Long
    )

    fun onClearPackageError(
        device: Device,
        attempt: Int,
        name: String,
        throwable: Throwable,
        durationMs: Long
    )

    fun onClearPackageFailure(
        device: Device,
        name: String,
        throwable: Throwable,
        durationMs: Long
    )
}
