package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

public interface AdbDeviceListListener {

    public fun onListSuccess(
        device: Device,
        remotePath: String,
        durationMs: Long
    )

    public fun onListError(
        device: Device,
        attempt: Int,
        remotePath: String,
        throwable: Throwable,
        durationMs: Long
    )

    public fun onListFailure(
        device: Device,
        remotePath: String,
        throwable: Throwable,
        durationMs: Long
    )
}
