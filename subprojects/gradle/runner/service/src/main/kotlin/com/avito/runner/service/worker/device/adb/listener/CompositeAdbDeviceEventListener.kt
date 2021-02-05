package com.avito.runner.service.worker.device.adb.listener

import java.nio.file.Path

internal class CompositeAdbDeviceEventListener(
    private val listeners: List<AdbDeviceEventsListener>
) : AdbDeviceEventsListener {

    override fun onGetSdkPropertySuccess(attempt: Int, api: Int) {
        listeners.forEach { it.onGetSdkPropertySuccess(attempt, api) }
    }

    override fun onGetSdkPropertyAttemptFail(attempt: Int) {
        listeners.forEach { it.onGetSdkPropertyAttemptFail(attempt) }
    }

    override fun onGetSdkPropertyFailure(throwable: Throwable) {
        listeners.forEach { it.onGetSdkPropertyFailure(throwable) }
    }

    override fun onInstallApplicationSuccess(attempt: Int, applicationPackage: String) {
        listeners.forEach { it.onInstallApplicationSuccess(attempt, applicationPackage) }
    }

    override fun onInstallApplicationAttemptFail(attempt: Int, applicationPackage: String) {
        listeners.forEach { it.onInstallApplicationAttemptFail(attempt, applicationPackage) }
    }

    override fun onInstallApplicationFailure(applicationPackage: String, throwable: Throwable) {
        listeners.forEach { it.onInstallApplicationFailure(applicationPackage, throwable) }
    }

    override fun onGetAliveDeviceSuccess(attempt: Int) {
        listeners.forEach { it.onGetAliveDeviceSuccess(attempt) }
    }

    override fun onGetAliveDeviceAttemptFail(attempt: Int) {
        listeners.forEach { it.onGetAliveDeviceAttemptFail(attempt) }
    }

    override fun onGetAliveDeviceFailed(throwable: Throwable) {
        listeners.forEach { it.onGetAliveDeviceFailed(throwable) }
    }

    override fun onClearPackageSuccess(attempt: Int, name: String) {
        listeners.forEach { it.onClearPackageSuccess(attempt, name) }
    }

    override fun onClearPackageAttemptFail(attempt: Int, name: String, throwable: Throwable) {
        listeners.forEach { it.onClearPackageAttemptFail(attempt, name, throwable) }
    }

    override fun onClearPackageFailure(name: String, throwable: Throwable) {
        listeners.forEach { it.onClearPackageFailure(name, throwable) }
    }

    override fun onPullSuccess(from: Path, to: Path) {
        listeners.forEach { it.onPullSuccess(from, to) }
    }

    override fun onPullAttemptFail(attempt: Int, from: Path, throwable: Throwable) {
        listeners.forEach { it.onPullAttemptFail(attempt, from, throwable) }
    }

    override fun onPullFailure(from: Path, throwable: Throwable) {
        listeners.forEach { it.onPullFailure(from, throwable) }
    }

    override fun onClearDirectorySuccess(remotePath: Path, output: String) {
        listeners.forEach { it.onClearDirectorySuccess(remotePath, output) }
    }

    override fun onClearDirectoryNothingDone(remotePath: Path) {
        listeners.forEach { it.onClearDirectoryNothingDone(remotePath) }
    }

    override fun onClearDirectoryAttemptFail(attempt: Int, remotePath: Path, throwable: Throwable) {
        listeners.forEach { it.onClearDirectoryAttemptFail(attempt, remotePath, throwable) }
    }

    override fun onClearDirectoryFailure(remotePath: Path, throwable: Throwable) {
        listeners.forEach { it.onClearDirectoryFailure(remotePath, throwable) }
    }

    override fun onListSuccess(remotePath: String) {
        listeners.forEach { it.onListSuccess(remotePath) }
    }

    override fun onListAttemptFail(attempt: Int, remotePath: String, throwable: Throwable) {
        listeners.forEach { it.onListAttemptFail(attempt, remotePath, throwable) }
    }

    override fun onListFailure(remotePath: String, throwable: Throwable) {
        listeners.forEach { it.onListFailure(remotePath, throwable) }
    }

    override fun onRunTestPassed(testName: String) {
        listeners.forEach { it.onRunTestPassed(testName) }
    }

    override fun onRunTestIgnored(testName: String) {
        listeners.forEach { it.onRunTestIgnored(testName) }
    }

    override fun onRunTestRunError(testName: String, errorMessage: String) {
        listeners.forEach { it.onRunTestRunError(testName, errorMessage) }
    }

    override fun onRunTestInfrastructureError(testName: String, errorMessage: String, throwable: Throwable?) {
        listeners.forEach { it.onRunTestInfrastructureError(testName, errorMessage, throwable) }
    }

    override fun onRunTestFailedOnStart(message: String) {
        listeners.forEach { it.onRunTestFailedOnStart(message) }
    }

    override fun onRunTestFailedOnInstrumentationParse(message: String, throwable: Throwable) {
        listeners.forEach { it.onRunTestFailedOnInstrumentationParse(message, throwable) }
    }
}
