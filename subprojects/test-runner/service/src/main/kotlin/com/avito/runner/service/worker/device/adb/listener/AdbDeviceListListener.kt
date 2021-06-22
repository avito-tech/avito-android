package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

interface AdbDeviceListListener {
    fun onListSuccess(
        device: Device,
        remotePath: String,
        durationMs: Long
    )

    fun onListError(
        device: Device,
        attempt: Int,
        remotePath: String,
        throwable: Throwable,
        durationMs: Long
    )

    fun onListFailure(
        device: Device,
        remotePath: String,
        throwable: Throwable,
        durationMs: Long
    )
}
