package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device

interface AdbDeviceRunTestListener {
    fun onRunTestPassed(
        device: Device,
        testName: String,
        durationMs: Long
    )

    fun onRunTestIgnored(
        device: Device,
        testName: String,
        durationMs: Long
    )

    fun onRunTestRunError(
        device: Device,
        testName: String,
        errorMessage: String,
        durationMs: Long
    )

    fun onRunTestInfrastructureError(
        device: Device,
        testName: String,
        errorMessage: String,
        throwable: Throwable?,
        durationMs: Long
    )

    fun onRunTestFailedOnStart(
        device: Device,
        message: String,
        durationMs: Long
    )

    fun onRunTestFailedOnInstrumentationParse(
        device: Device,
        message: String,
        throwable: Throwable,
        durationMs: Long
    )
}
