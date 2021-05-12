package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

interface AdbDeviceEventsListener {

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

    fun onInstallApplicationSuccess(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        durationMs: Long
    )

    fun onInstallApplicationError(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    )

    fun onInstallApplicationFailure(
        device: Device,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    )

    fun onGetAliveDeviceSuccess(
        device: Device,
        attempt: Int,
        durationMs: Long
    )

    fun onGetAliveDeviceError(
        device: Device,
        attempt: Int,
        durationMs: Long
    )

    fun onGetAliveDeviceFailed(
        device: Device,
        throwable: Throwable,
        durationMs: Long
    )

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
