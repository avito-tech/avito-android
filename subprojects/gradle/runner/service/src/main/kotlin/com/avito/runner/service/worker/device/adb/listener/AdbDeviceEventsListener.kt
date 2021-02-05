package com.avito.runner.service.worker.device.adb.listener

import java.nio.file.Path

interface AdbDeviceEventsListener {
    fun getSdkPropertySuccess(attempt: Int, api: Int)
    fun getSdkPropertyAttemptFail(attempt: Int)
    fun getSdkPropertyFailure(throwable: Throwable)

    fun installApplicationSuccess(attempt: Int, applicationPackage: String)
    fun installApplicationAttemptFail(attempt: Int, applicationPackage: String)
    fun installApplicationFailure(applicationPackage: String, throwable: Throwable)

    fun getAliveDeviceSuccess(attempt: Int)
    fun getAliveDeviceAttemptFail(attempt: Int)
    fun getAliveDeviceFailed(throwable: Throwable)

    fun clearPackageSuccess(attempt: Int, name: String)
    fun clearPackageAttemptFail(attempt: Int, name: String, throwable: Throwable)
    fun clearPackageFailure(name: String, throwable: Throwable)

    fun pullSuccess(from: Path, to: Path)
    fun pullAttemptFail(attempt: Int, from: Path, throwable: Throwable)
    fun pullFailure(from: Path, throwable: Throwable)

    fun clearDirectorySuccess(remotePath: Path, output: String)
    fun clearDirectoryNothingDone(remotePath: Path)
    fun clearDirectoryAttemptFail(attempt: Int, remotePath: Path, throwable: Throwable)
    fun clearDirectoryFailure(remotePath: Path, throwable: Throwable)

    fun listSuccess(remotePath: String)
    fun listAttemptFail(attempt: Int, remotePath: String, throwable: Throwable)
    fun listFailure(remotePath: String, throwable: Throwable)

    fun runTestPassed(testName: String)
    fun runTestIgnored(testName: String)
    fun runTestRunError(testName: String, errorMessage: String)
    fun runTestInfrastructureError(testName: String, errorMessage: String, throwable: Throwable?)
    fun runTestFailedOnStart(message: String)
    fun runTestFailedOnInstrumentationParse(message: String, throwable: Throwable)
}
