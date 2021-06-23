package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

interface AdbDeviceClearDirectoryListener {
    fun onClearDirectorySuccess(
        device: Device,
        remotePath: Path,
        output: String,
        durationMs: Long
    )

    fun onClearDirectoryError(
        device: Device,
        attempt: Int,
        remotePath: Path,
        throwable: Throwable,
        durationMs: Long
    )

    fun onClearDirectoryFailure(
        device: Device,
        remotePath: Path,
        throwable: Throwable,
        durationMs: Long
    )
}
