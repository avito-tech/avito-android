package com.avito.runner.service.worker.device.adb.listener

import java.nio.file.Path

interface AdbDeviceEventsListener {
    fun onGetSdkPropertySuccess(attempt: Int, api: Int)
    fun onGetSdkPropertyAttemptFail(attempt: Int)
    fun onGetSdkPropertyFailure(throwable: Throwable)

    fun onInstallApplicationSuccess(attempt: Int, applicationPackage: String)
    fun onInstallApplicationAttemptFail(attempt: Int, applicationPackage: String)
    fun onInstallApplicationFailure(applicationPackage: String, throwable: Throwable)

    fun onGetAliveDeviceSuccess(attempt: Int)
    fun onGetAliveDeviceAttemptFail(attempt: Int)
    fun onGetAliveDeviceFailed(throwable: Throwable)

    fun onClearPackageSuccess(attempt: Int, name: String)
    fun onClearPackageAttemptFail(attempt: Int, name: String, throwable: Throwable)
    fun onClearPackageFailure(name: String, throwable: Throwable)

    fun onPullSuccess(from: Path, to: Path)
    fun onPullAttemptFail(attempt: Int, from: Path, throwable: Throwable)
    fun onPullFailure(from: Path, throwable: Throwable)

    fun onClearDirectorySuccess(remotePath: Path, output: String)
    fun onClearDirectoryNothingDone(remotePath: Path)
    fun onClearDirectoryAttemptFail(attempt: Int, remotePath: Path, throwable: Throwable)
    fun onClearDirectoryFailure(remotePath: Path, throwable: Throwable)

    fun onListSuccess(remotePath: String)
    fun onListAttemptFail(attempt: Int, remotePath: String, throwable: Throwable)
    fun onListFailure(remotePath: String, throwable: Throwable)

    fun onRunTestPassed(testName: String)
    fun onRunTestIgnored(testName: String)
    fun onRunTestRunError(testName: String, errorMessage: String)
    fun onRunTestInfrastructureError(testName: String, errorMessage: String, throwable: Throwable?)
    fun onRunTestFailedOnStart(message: String)
    fun onRunTestFailedOnInstrumentationParse(message: String, throwable: Throwable)
}
