package com.avito.runner.service.worker.device.adb.listener

import com.avito.logger.Logger
import java.nio.file.Path

internal class AdbDeviceEventsLogger(private val logger: Logger) : AdbDeviceEventsListener {

    override fun onGetSdkPropertySuccess(attempt: Int, api: Int) {
        logger.debug("Got ro.build.version.sdk = $api")
    }

    override fun onGetSdkPropertyAttemptFail(attempt: Int) {
        logger.debug("Attempt $attempt: reading ro.build.version.sdk failed")
    }

    override fun onGetSdkPropertyFailure(throwable: Throwable) {
        logger.warn("Failed reading ro.build.version.sdk", throwable)
    }

    override fun onInstallApplicationSuccess(attempt: Int, applicationPackage: String) {
        logger.debug("Attempt $attempt: application $applicationPackage installed")
    }

    override fun onInstallApplicationAttemptFail(attempt: Int, applicationPackage: String) {
        logger.debug("Attempt $attempt: failed to install application $applicationPackage")
    }

    override fun onInstallApplicationFailure(applicationPackage: String, throwable: Throwable) {
        logger.warn("Failed to install application package: $applicationPackage", throwable)
    }

    override fun onGetAliveDeviceSuccess(attempt: Int) {
        logger.debug("Device status: alive")
    }

    override fun onGetAliveDeviceAttemptFail(attempt: Int) {
        logger.debug("Attempt $attempt: failed to determine the device status")
    }

    override fun onGetAliveDeviceFailed(throwable: Throwable) {
        logger.warn("Failed reading device status", throwable)
    }

    override fun onClearPackageSuccess(attempt: Int, name: String) {
        logger.debug("Attempt: $attempt: clear package $name completed")
    }

    override fun onClearPackageAttemptFail(attempt: Int, name: String, throwable: Throwable) {
        logger.debug("Attempt $attempt: failed to clear package $name")
    }

    override fun onClearPackageFailure(name: String, throwable: Throwable) {
        logger.warn("Failed to clear package $name", throwable)
    }

    override fun onPullSuccess(from: Path, to: Path) {
        logger.debug("Pull success from: $from to $to")
    }

    override fun onPullAttemptFail(attempt: Int, from: Path, throwable: Throwable) {
        logger.debug("Attempt $attempt: failed to pull $from")
    }

    override fun onPullFailure(from: Path, throwable: Throwable) {
        logger.warn("Failed pulling data $from", throwable)
    }

    override fun onClearDirectorySuccess(remotePath: Path, output: String) {
        logger.debug("Successfully cleared $remotePath. ($output)")
    }

    override fun onClearDirectoryNothingDone(remotePath: Path) {
        logger.debug("Nothing cleared in $remotePath")
    }

    override fun onClearDirectoryAttemptFail(attempt: Int, remotePath: Path, throwable: Throwable) {
        logger.debug("Attempt $attempt: failed to clear $remotePath")
    }

    override fun onClearDirectoryFailure(remotePath: Path, throwable: Throwable) {
        logger.warn("Failed clearing directory $remotePath", throwable)
    }

    override fun onListSuccess(remotePath: String) {
        logger.debug("Listing $remotePath success")
    }

    override fun onListAttemptFail(attempt: Int, remotePath: String, throwable: Throwable) {
        logger.debug("Attempt $attempt: failed to list directory $remotePath")
    }

    override fun onListFailure(remotePath: String, throwable: Throwable) {
        logger.warn("Failed listing path $remotePath", throwable)
    }

    override fun onRunTestFailedOnStart(message: String) {
        logger.warn("Run test failed on start: $message")
    }

    override fun onRunTestFailedOnInstrumentationParse(message: String, throwable: Throwable) {
        logger.warn("Run test failed on instrumentation parse: $message", throwable)
    }

    override fun onRunTestPassed(testName: String) {
        logger.debug("Run test passed: $testName")
    }

    override fun onRunTestIgnored(testName: String) {
        logger.debug("Run test ignored: $testName")
    }

    override fun onRunTestRunError(testName: String, errorMessage: String) {
        logger.warn("Run test error: $testName ($errorMessage)")
    }

    override fun onRunTestInfrastructureError(testName: String, errorMessage: String, throwable: Throwable?) {
        logger.warn("Run test infrastructure error: $testName ($errorMessage)", throwable)
    }
}
