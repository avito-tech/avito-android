package com.avito.runner.service.worker.device.adb.listener

import com.avito.logger.Logger
import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

internal class AdbDeviceEventsLogger(private val logger: Logger) : AdbDeviceEventsListener {

    override fun onGetSdkPropertySuccess(attempt: Int, api: Int, durationMs: Long) {
        logger.debug("Got ro.build.version.sdk = $api")
    }

    override fun onGetSdkPropertyError(attempt: Int, durationMs: Long) {
        logger.debug("Attempt $attempt: reading ro.build.version.sdk failed")
    }

    override fun onGetSdkPropertyFailure(throwable: Throwable, durationMs: Long) {
        logger.warn("Failed reading ro.build.version.sdk", throwable)
    }

    override fun onInstallApplicationSuccess(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        durationMs: Long
    ) {
        logger.debug("Attempt $attempt: application $applicationPackage installed")
    }

    override fun onInstallApplicationError(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.debug("Attempt $attempt: failed to install application $applicationPackage")
    }

    override fun onInstallApplicationFailure(
        device: Device,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.warn("Failed to install application package: $applicationPackage", throwable)
    }

    override fun onGetAliveDeviceSuccess(
        device: Device,
        attempt: Int,
        durationMs: Long
    ) {
        logger.debug("Device status: alive")
    }

    override fun onGetAliveDeviceError(
        device: Device,
        attempt: Int,
        durationMs: Long
    ) {
        logger.debug("Attempt $attempt: failed to determine the device status")
    }

    override fun onGetAliveDeviceFailed(
        device: Device,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.warn("Failed reading device status", throwable)
    }

    override fun onClearPackageSuccess(
        device: Device,
        attempt: Int,
        name: String,
        durationMs: Long
    ) {
        logger.debug("Attempt: $attempt: clear package $name completed")
    }

    override fun onClearPackageError(
        device: Device,
        attempt: Int,
        name: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.debug("Attempt $attempt: failed to clear package $name")
    }

    override fun onClearPackageFailure(
        device: Device,
        name: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.warn("Failed to clear package $name", throwable)
    }

    override fun onPullSuccess(
        device: Device,
        from: Path,
        to: Path,
        durationMs: Long
    ) {
        logger.debug("Pull success from: $from to $to")
    }

    override fun onPullError(
        device: Device,
        attempt: Int,
        from: Path,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.debug("Attempt $attempt: failed to pull $from")
    }

    override fun onPullFailure(
        device: Device,
        from: Path,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.warn("Failed pulling data $from", throwable)
    }

    override fun onClearDirectorySuccess(
        device: Device,
        remotePath: Path,
        output: String,
        durationMs: Long
    ) {
        logger.debug("Successfully cleared $remotePath. ($output)")
    }

    override fun onClearDirectoryError(
        device: Device,
        attempt: Int,
        remotePath: Path,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.debug("Attempt $attempt: failed to clear $remotePath")
    }

    override fun onClearDirectoryFailure(
        device: Device,
        remotePath: Path,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.warn("Failed clearing directory $remotePath", throwable)
    }

    override fun onListSuccess(
        device: Device,
        remotePath: String,
        durationMs: Long
    ) {
        logger.debug("Listing $remotePath success")
    }

    override fun onListError(
        device: Device,
        attempt: Int,
        remotePath: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.debug("Attempt $attempt: failed to list directory $remotePath")
    }

    override fun onListFailure(
        device: Device,
        remotePath: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.warn("Failed listing path $remotePath", throwable)
    }

    override fun onRunTestFailedOnStart(
        device: Device,
        message: String,
        durationMs: Long
    ) {
        logger.warn("Run test failed on start: $message")
    }

    override fun onRunTestFailedOnInstrumentationParse(
        device: Device,
        message: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        logger.warn("Run test failed on instrumentation parse: $message", throwable)
    }

    override fun onRunTestPassed(
        device: Device,
        testName: String,
        durationMs: Long
    ) {
        logger.debug("Run test passed: $testName")
    }

    override fun onRunTestIgnored(
        device: Device,
        testName: String,
        durationMs: Long
    ) {
        logger.debug("Run test ignored: $testName")
    }

    override fun onRunTestRunError(
        device: Device,
        testName: String,
        errorMessage: String,
        durationMs: Long
    ) {
        logger.warn("Run test error: $testName ($errorMessage)")
    }

    override fun onRunTestInfrastructureError(
        device: Device,
        testName: String,
        errorMessage: String,
        throwable: Throwable?,
        durationMs: Long
    ) {
        logger.warn("Run test infrastructure error: $testName ($errorMessage)", throwable)
    }
}
