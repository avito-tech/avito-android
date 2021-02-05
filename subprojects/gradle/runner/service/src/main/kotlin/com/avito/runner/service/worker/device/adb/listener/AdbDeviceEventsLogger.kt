package com.avito.runner.service.worker.device.adb.listener

import com.avito.logger.Logger
import java.nio.file.Path

internal class AdbDeviceEventsLogger(private val logger: Logger) : AdbDeviceEventsListener {

    override fun getSdkPropertySuccess(attempt: Int, api: Int) {
        logger.debug("Got ro.build.version.sdk = $api")
    }

    override fun getSdkPropertyAttemptFail(attempt: Int) {
        logger.debug("Attempt $attempt: reading ro.build.version.sdk failed")
    }

    override fun getSdkPropertyFailure(throwable: Throwable) {
        logger.warn("Failed reading ro.build.version.sdk", throwable)
    }

    override fun installApplicationSuccess(attempt: Int, applicationPackage: String) {
        logger.debug("Attempt $attempt: application $applicationPackage installed")
    }

    override fun installApplicationAttemptFail(attempt: Int, applicationPackage: String) {
        logger.debug("Attempt $attempt: failed to install application $applicationPackage")
    }

    override fun installApplicationFailure(applicationPackage: String, throwable: Throwable) {
        logger.warn("Failed to install application package: $applicationPackage", throwable)
    }

    override fun getAliveDeviceSuccess(attempt: Int) {
        logger.debug("Device status: alive")
    }

    override fun getAliveDeviceAttemptFail(attempt: Int) {
        logger.debug("Attempt $attempt: failed to determine the device status")
    }

    override fun getAliveDeviceFailed(throwable: Throwable) {
        logger.warn("Failed reading device status", throwable)
    }

    override fun clearPackageSuccess(attempt: Int, name: String) {
        logger.debug("Attempt: $attempt: clear package $name completed")
    }

    override fun clearPackageAttemptFail(attempt: Int, name: String, throwable: Throwable) {
        logger.debug("Attempt $attempt: failed to clear package $name")
    }

    override fun clearPackageFailure(name: String, throwable: Throwable) {
        logger.warn("Failed to clear package $name", throwable)
    }

    override fun pullSuccess(from: Path, to: Path) {
        logger.debug("Pull success from: $from to $to")
    }

    override fun pullAttemptFail(attempt: Int, from: Path, throwable: Throwable) {
        logger.debug("Attempt $attempt: failed to pull $from")
    }

    override fun pullFailure(from: Path, throwable: Throwable) {
        logger.warn("Failed pulling data $from", throwable)
    }

    override fun clearDirectorySuccess(remotePath: Path, output: String) {
        logger.debug("Successfully cleared $remotePath. ($output)")
    }

    override fun clearDirectoryNothingDone(remotePath: Path) {
        logger.debug("Nothing cleared in $remotePath")
    }

    override fun clearDirectoryAttemptFail(attempt: Int, remotePath: Path, throwable: Throwable) {
        logger.debug("Attempt $attempt: failed to clear $remotePath")
    }

    override fun clearDirectoryFailure(remotePath: Path, throwable: Throwable) {
        logger.warn("Failed clearing directory $remotePath", throwable)
    }

    override fun listSuccess(remotePath: String) {
        logger.debug("Listing $remotePath success")
    }

    override fun listAttemptFail(attempt: Int, remotePath: String, throwable: Throwable) {
        logger.debug("Attempt $attempt: failed to list directory $remotePath")
    }

    override fun listFailure(remotePath: String, throwable: Throwable) {
        logger.warn("Failed listing path $remotePath", throwable)
    }

    override fun runTestFailedOnStart(message: String) {
        logger.warn("Run test failed on start: $message")
    }

    override fun runTestFailedOnInstrumentationParse(message: String, throwable: Throwable) {
        logger.warn("Run test failed on instrumentation parse: $message", throwable)
    }

    override fun runTestPassed(testName: String) {
        logger.debug("Run test passed: $testName")
    }

    override fun runTestIgnored(testName: String) {
        logger.debug("Run test ignored: $testName")
    }

    override fun runTestRunError(testName: String, errorMessage: String) {
        logger.warn("Run test error: $testName ($errorMessage)")
    }

    override fun runTestInfrastructureError(testName: String, errorMessage: String, throwable: Throwable?) {
        logger.warn("Run test infrastructure error: $testName ($errorMessage)", throwable)
    }
}
