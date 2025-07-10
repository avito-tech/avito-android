package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

public interface AdbDeviceRunTestListener {

    public fun onRunTestPassed(
        device: Device,
        testName: String,
        durationMs: Long
    )

    public fun onRunTestIgnored(
        device: Device,
        testName: String,
        durationMs: Long
    )

    public fun onRunTestRunError(
        device: Device,
        testName: String,
        errorMessage: String,
        durationMs: Long
    )

    public fun onRunTestInfrastructureError(
        device: Device,
        testName: String,
        errorMessage: String,
        throwable: Throwable?,
        durationMs: Long
    )

    public fun onRunTestFailedOnStart(
        device: Device,
        message: String,
        durationMs: Long
    )

    public fun onRunTestFailedOnInstrumentationParse(
        device: Device,
        message: String,
        throwable: Throwable,
        durationMs: Long
    )
}
