package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

internal class CompositeAdbDeviceEventListener(
    private val listeners: List<AdbDeviceEventsListener>
) : AdbDeviceEventsListener {

    override fun onGetSdkPropertySuccess(attempt: Int, api: Int) {
        listeners.forEach { it.onGetSdkPropertySuccess(attempt, api) }
    }

    override fun onGetSdkPropertyError(attempt: Int) {
        listeners.forEach { it.onGetSdkPropertyError(attempt) }
    }

    override fun onGetSdkPropertyFailure(throwable: Throwable) {
        listeners.forEach { it.onGetSdkPropertyFailure(throwable) }
    }

    override fun onInstallApplicationSuccess(device: Device, attempt: Int, applicationPackage: String) {
        listeners.forEach { it.onInstallApplicationSuccess(device, attempt, applicationPackage) }
    }

    override fun onInstallApplicationError(device: Device, attempt: Int, applicationPackage: String) {
        listeners.forEach { it.onInstallApplicationError(device, attempt, applicationPackage) }
    }

    override fun onInstallApplicationFailure(device: Device, applicationPackage: String, throwable: Throwable) {
        listeners.forEach { it.onInstallApplicationFailure(device, applicationPackage, throwable) }
    }

    override fun onGetAliveDeviceSuccess(device: Device, attempt: Int) {
        listeners.forEach { it.onGetAliveDeviceSuccess(device, attempt) }
    }

    override fun onGetAliveDeviceError(device: Device, attempt: Int) {
        listeners.forEach { it.onGetAliveDeviceError(device, attempt) }
    }

    override fun onGetAliveDeviceFailed(device: Device, throwable: Throwable) {
        listeners.forEach { it.onGetAliveDeviceFailed(device, throwable) }
    }

    override fun onClearPackageSuccess(device: Device, attempt: Int, name: String) {
        listeners.forEach { it.onClearPackageSuccess(device, attempt, name) }
    }

    override fun onClearPackageError(device: Device, attempt: Int, name: String, throwable: Throwable) {
        listeners.forEach { it.onClearPackageError(device, attempt, name, throwable) }
    }

    override fun onClearPackageFailure(device: Device, name: String, throwable: Throwable) {
        listeners.forEach { it.onClearPackageFailure(device, name, throwable) }
    }

    override fun onPullSuccess(device: Device, from: Path, to: Path) {
        listeners.forEach { it.onPullSuccess(device, from, to) }
    }

    override fun onPullError(device: Device, attempt: Int, from: Path, throwable: Throwable) {
        listeners.forEach { it.onPullError(device, attempt, from, throwable) }
    }

    override fun onPullFailure(device: Device, from: Path, throwable: Throwable) {
        listeners.forEach { it.onPullFailure(device, from, throwable) }
    }

    override fun onClearDirectorySuccess(device: Device, remotePath: Path, output: String) {
        listeners.forEach { it.onClearDirectorySuccess(device, remotePath, output) }
    }

    override fun onClearDirectoryError(device: Device, attempt: Int, remotePath: Path, throwable: Throwable) {
        listeners.forEach { it.onClearDirectoryError(device, attempt, remotePath, throwable) }
    }

    override fun onClearDirectoryFailure(device: Device, remotePath: Path, throwable: Throwable) {
        listeners.forEach { it.onClearDirectoryFailure(device, remotePath, throwable) }
    }

    override fun onListSuccess(device: Device, remotePath: String) {
        listeners.forEach { it.onListSuccess(device, remotePath) }
    }

    override fun onListError(device: Device, attempt: Int, remotePath: String, throwable: Throwable) {
        listeners.forEach { it.onListError(device, attempt, remotePath, throwable) }
    }

    override fun onListFailure(device: Device, remotePath: String, throwable: Throwable) {
        listeners.forEach { it.onListFailure(device, remotePath, throwable) }
    }

    override fun onRunTestPassed(device: Device, testName: String) {
        listeners.forEach { it.onRunTestPassed(device, testName) }
    }

    override fun onRunTestIgnored(device: Device, testName: String) {
        listeners.forEach { it.onRunTestIgnored(device, testName) }
    }

    override fun onRunTestRunError(device: Device, testName: String, errorMessage: String) {
        listeners.forEach { it.onRunTestRunError(device, testName, errorMessage) }
    }

    override fun onRunTestInfrastructureError(
        device: Device,
        testName: String,
        errorMessage: String,
        throwable: Throwable?
    ) {
        listeners.forEach { it.onRunTestInfrastructureError(device, testName, errorMessage, throwable) }
    }

    override fun onRunTestFailedOnStart(device: Device, message: String) {
        listeners.forEach { it.onRunTestFailedOnStart(device, message) }
    }

    override fun onRunTestFailedOnInstrumentationParse(device: Device, message: String, throwable: Throwable) {
        listeners.forEach { it.onRunTestFailedOnInstrumentationParse(device, message, throwable) }
    }
}
