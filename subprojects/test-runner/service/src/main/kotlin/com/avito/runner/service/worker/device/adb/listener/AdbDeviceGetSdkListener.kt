package com.avito.runner.service.worker.device.adb.listener

public interface AdbDeviceGetSdkListener {

    public fun onGetSdkPropertySuccess(
        attempt: Int,
        api: Int,
        durationMs: Long
    )

    public fun onGetSdkPropertyError(
        attempt: Int,
        durationMs: Long
    )

    public fun onGetSdkPropertyFailure(
        throwable: Throwable,
        durationMs: Long
    )
}
