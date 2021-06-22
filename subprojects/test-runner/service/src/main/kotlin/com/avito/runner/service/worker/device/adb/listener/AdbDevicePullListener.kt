package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

interface AdbDevicePullListener {
    fun onPullSuccess(
        device: Device,
        from: Path,
        to: Path,
        durationMs: Long
    )

    fun onPullError(
        device: Device,
        attempt: Int,
        from: Path,
        throwable: Throwable,
        durationMs: Long
    )

    fun onPullFailure(
        device: Device,
        from: Path,
        throwable: Throwable,
        durationMs: Long
    )
}
