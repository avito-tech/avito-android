package com.avito.runner.service.worker.device.adb.listener

import java.nio.file.Path

internal class CompositeAdbDeviceEventListener(
    private val listeners: List<AdbDeviceEventsListener>
) : AdbDeviceEventsListener {

    override fun getSdkPropertySuccess(attempt: Int, api: Int) {
        listeners.forEach { it.getSdkPropertySuccess(attempt, api) }
    }

    override fun getSdkPropertyAttemptFail(attempt: Int) {
        listeners.forEach { it.getSdkPropertyAttemptFail(attempt) }
    }

    override fun getSdkPropertyFailure(throwable: Throwable) {
        listeners.forEach { it.getSdkPropertyFailure(throwable) }
    }

    override fun installApplicationSuccess(attempt: Int, applicationPackage: String) {
        listeners.forEach { it.installApplicationSuccess(attempt, applicationPackage) }
    }

    override fun installApplicationAttemptFail(attempt: Int, applicationPackage: String) {
        listeners.forEach { it.installApplicationAttemptFail(attempt, applicationPackage) }
    }

    override fun installApplicationFailure(applicationPackage: String, throwable: Throwable) {
        listeners.forEach { it.installApplicationFailure(applicationPackage, throwable) }
    }

    override fun getAliveDeviceSuccess(attempt: Int) {
        listeners.forEach { it.getAliveDeviceSuccess(attempt) }
    }

    override fun getAliveDeviceAttemptFail(attempt: Int) {
        listeners.forEach { it.getAliveDeviceAttemptFail(attempt) }
    }

    override fun getAliveDeviceFailed(throwable: Throwable) {
        listeners.forEach { it.getAliveDeviceFailed(throwable) }
    }

    override fun clearPackageSuccess(attempt: Int, name: String) {
        listeners.forEach { it.clearPackageSuccess(attempt, name) }
    }

    override fun clearPackageAttemptFail(attempt: Int, name: String, throwable: Throwable) {
        listeners.forEach { it.clearPackageAttemptFail(attempt, name, throwable) }
    }

    override fun clearPackageFailure(name: String, throwable: Throwable) {
        listeners.forEach { it.clearPackageFailure(name, throwable) }
    }

    override fun pullSuccess(from: Path, to: Path) {
        listeners.forEach { it.pullSuccess(from, to) }
    }

    override fun pullAttemptFail(attempt: Int, from: Path, throwable: Throwable) {
        listeners.forEach { it.pullAttemptFail(attempt, from, throwable) }
    }

    override fun pullFailure(from: Path, throwable: Throwable) {
        listeners.forEach { it.pullFailure(from, throwable) }
    }

    override fun clearDirectorySuccess(remotePath: Path, output: String) {
        listeners.forEach { it.clearDirectorySuccess(remotePath, output) }
    }

    override fun clearDirectoryNothingDone(remotePath: Path) {
        listeners.forEach { it.clearDirectoryNothingDone(remotePath) }
    }

    override fun clearDirectoryAttemptFail(attempt: Int, remotePath: Path, throwable: Throwable) {
        listeners.forEach { it.clearDirectoryAttemptFail(attempt, remotePath, throwable) }
    }

    override fun clearDirectoryFailure(remotePath: Path, throwable: Throwable) {
        listeners.forEach { it.clearDirectoryFailure(remotePath, throwable) }
    }

    override fun listSuccess(remotePath: String) {
        listeners.forEach { it.listSuccess(remotePath) }
    }

    override fun listAttemptFail(attempt: Int, remotePath: String, throwable: Throwable) {
        listeners.forEach { it.listAttemptFail(attempt, remotePath, throwable) }
    }

    override fun listFailure(remotePath: String, throwable: Throwable) {
        listeners.forEach { it.listFailure(remotePath, throwable) }
    }

    override fun runTestPassed(testName: String) {
        listeners.forEach { it.runTestPassed(testName) }
    }

    override fun runTestIgnored(testName: String) {
        listeners.forEach { it.runTestIgnored(testName) }
    }

    override fun runTestRunError(testName: String, errorMessage: String) {
        listeners.forEach { it.runTestRunError(testName, errorMessage) }
    }

    override fun runTestInfrastructureError(testName: String, errorMessage: String, throwable: Throwable?) {
        listeners.forEach { it.runTestInfrastructureError(testName, errorMessage, throwable) }
    }

    override fun runTestFailedOnStart(message: String) {
        listeners.forEach { it.runTestFailedOnStart(message) }
    }

    override fun runTestFailedOnInstrumentationParse(message: String, throwable: Throwable) {
        listeners.forEach { it.runTestFailedOnInstrumentationParse(message, throwable) }
    }
}
