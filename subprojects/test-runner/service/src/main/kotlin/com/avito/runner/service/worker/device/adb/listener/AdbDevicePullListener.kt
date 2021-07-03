package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

public interface AdbDevicePullListener {

    public fun onPullSuccess(
        device: Device,
        from: Path,
        to: Path,
        durationMs: Long
    )

    public fun onPullError(
        device: Device,
        attempt: Int,
        from: Path,
        throwable: Throwable,
        durationMs: Long
    )

    public fun onPullFailure(
        device: Device,
        from: Path,
        throwable: Throwable,
        durationMs: Long
    )
}
