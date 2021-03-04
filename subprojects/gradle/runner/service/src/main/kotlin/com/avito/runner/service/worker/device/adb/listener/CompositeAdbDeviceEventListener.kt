package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

internal class CompositeAdbDeviceEventListener(
    private val listeners: List<AdbDeviceEventsListener>
) : AdbDeviceEventsListener {

    override fun onGetSdkPropertySuccess(attempt: Int, api: Int, durationMs: Long) {
        listeners.forEach { it.onGetSdkPropertySuccess(attempt, api, durationMs) }
    }

    override fun onGetSdkPropertyError(attempt: Int, durationMs: Long) {
        listeners.forEach { it.onGetSdkPropertyError(attempt, durationMs) }
    }

    override fun onGetSdkPropertyFailure(throwable: Throwable, durationMs: Long) {
        listeners.forEach { it.onGetSdkPropertyFailure(throwable, durationMs) }
    }

    override fun onInstallApplicationSuccess(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        durationMs: Long
    ) {
        listeners.forEach { it.onInstallApplicationSuccess(device, attempt, applicationPackage, durationMs) }
    }

    override fun onInstallApplicationError(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        listeners.forEach { it.onInstallApplicationError(device, attempt, applicationPackage, throwable, durationMs) }
    }

    override fun onInstallApplicationFailure(
        device: Device,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        listeners.forEach { it.onInstallApplicationFailure(device, applicationPackage, throwable, durationMs) }
    }

    override fun onGetAliveDeviceSuccess(device: Device, attempt: Int, durationMs: Long) {
        listeners.forEach { it.onGetAliveDeviceSuccess(device, attempt, durationMs) }
    }

    override fun onGetAliveDeviceError(device: Device, attempt: Int, durationMs: Long) {
        listeners.forEach { it.onGetAliveDeviceError(device, attempt, durationMs) }
    }

    override fun onGetAliveDeviceFailed(device: Device, throwable: Throwable, durationMs: Long) {
        listeners.forEach { it.onGetAliveDeviceFailed(device, throwable, durationMs) }
    }

    override fun onClearPackageSuccess(device: Device, attempt: Int, name: String, durationMs: Long) {
        listeners.forEach { it.onClearPackageSuccess(device, attempt, name, durationMs) }
    }

    override fun onClearPackageError(
        device: Device,
        attempt: Int,
        name: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        listeners.forEach { it.onClearPackageError(device, attempt, name, throwable, durationMs) }
    }

    override fun onClearPackageFailure(device: Device, name: String, throwable: Throwable, durationMs: Long) {
        listeners.forEach { it.onClearPackageFailure(device, name, throwable, durationMs) }
    }

    override fun onPullSuccess(device: Device, from: Path, to: Path, durationMs: Long) {
        listeners.forEach { it.onPullSuccess(device, from, to, durationMs) }
    }

    override fun onPullError(device: Device, attempt: Int, from: Path, throwable: Throwable, durationMs: Long) {
        listeners.forEach { it.onPullError(device, attempt, from, throwable, durationMs) }
    }

    override fun onPullFailure(device: Device, from: Path, throwable: Throwable, durationMs: Long) {
        listeners.forEach { it.onPullFailure(device, from, throwable, durationMs) }
    }

    override fun onClearDirectorySuccess(device: Device, remotePath: Path, output: String, durationMs: Long) {
        listeners.forEach { it.onClearDirectorySuccess(device, remotePath, output, durationMs) }
    }

    override fun onClearDirectoryError(
        device: Device,
        attempt: Int,
        remotePath: Path,
        throwable: Throwable,
        durationMs: Long
    ) {
        listeners.forEach { it.onClearDirectoryError(device, attempt, remotePath, throwable, durationMs) }
    }

    override fun onClearDirectoryFailure(device: Device, remotePath: Path, throwable: Throwable, durationMs: Long) {
        listeners.forEach { it.onClearDirectoryFailure(device, remotePath, throwable, durationMs) }
    }

    override fun onListSuccess(device: Device, remotePath: String, durationMs: Long) {
        listeners.forEach { it.onListSuccess(device, remotePath, durationMs) }
    }

    override fun onListError(device: Device, attempt: Int, remotePath: String, throwable: Throwable, durationMs: Long) {
        listeners.forEach { it.onListError(device, attempt, remotePath, throwable, durationMs) }
    }

    override fun onListFailure(device: Device, remotePath: String, throwable: Throwable, durationMs: Long) {
        listeners.forEach { it.onListFailure(device, remotePath, throwable, durationMs) }
    }

    override fun onRunTestPassed(device: Device, testName: String, durationMs: Long) {
        listeners.forEach { it.onRunTestPassed(device, testName, durationMs) }
    }

    override fun onRunTestIgnored(device: Device, testName: String, durationMs: Long) {
        listeners.forEach { it.onRunTestIgnored(device, testName, durationMs) }
    }

    override fun onRunTestRunError(device: Device, testName: String, errorMessage: String, durationMs: Long) {
        listeners.forEach { it.onRunTestRunError(device, testName, errorMessage, durationMs) }
    }

    override fun onRunTestInfrastructureError(
        device: Device,
        testName: String,
        errorMessage: String,
        throwable: Throwable?,
        durationMs: Long
    ) {
        listeners.forEach {
            it.onRunTestInfrastructureError(
                device,
                testName,
                errorMessage,
                throwable,
                durationMs
            )
        }
    }

    override fun onRunTestFailedOnStart(device: Device, message: String, durationMs: Long) {
        listeners.forEach { it.onRunTestFailedOnStart(device, message, durationMs) }
    }

    override fun onRunTestFailedOnInstrumentationParse(
        device: Device,
        message: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        listeners.forEach { it.onRunTestFailedOnInstrumentationParse(device, message, throwable, durationMs) }
    }
}
