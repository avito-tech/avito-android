package com.avito.runner.service.worker.device.adb.listener

interface AdbDeviceGetSdkListener {

    fun onGetSdkPropertySuccess(
        attempt: Int,
        api: Int,
        durationMs: Long
    )

    fun onGetSdkPropertyError(
        attempt: Int,
        durationMs: Long
    )

    fun onGetSdkPropertyFailure(
        throwable: Throwable,
        durationMs: Long
    )
}
