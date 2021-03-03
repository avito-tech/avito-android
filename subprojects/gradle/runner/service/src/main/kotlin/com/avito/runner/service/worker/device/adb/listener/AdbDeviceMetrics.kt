package com.avito.runner.service.worker.device.adb.listener

import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric
import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

class AdbDeviceMetrics(
    private val statsDSender: StatsDSender,
    runnerPrefix: SeriesName
) : AdbDeviceEventsListener {

    private val prefix = runnerPrefix.append("adb")

    override fun onGetSdkPropertySuccess(attempt: Int, api: Int, durationMs: Long) {
        statsDSender.send(TimeMetric(prefix.append("get-sdk-property", "success"), durationMs))
    }

    override fun onGetSdkPropertyError(attempt: Int, durationMs: Long) {
        statsDSender.send(TimeMetric(prefix.append("get-sdk-property", "error"), durationMs))
    }

    override fun onGetSdkPropertyFailure(throwable: Throwable, durationMs: Long) {
        statsDSender.send(TimeMetric(prefix.append("get-sdk-property", "failure"), durationMs))
    }

    override fun onInstallApplicationSuccess(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        durationMs: Long
    ) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("install-application", "success"), durationMs))
    }

    override fun onInstallApplicationError(
        device: Device,
        attempt: Int,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("install-application", "error"), durationMs))
    }

    override fun onInstallApplicationFailure(
        device: Device,
        applicationPackage: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("install-application", "failure"), durationMs))
    }

    override fun onGetAliveDeviceSuccess(device: Device, attempt: Int, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("get-alive-device", "success"), durationMs))
    }

    override fun onGetAliveDeviceError(device: Device, attempt: Int, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("get-alive-device", "error"), durationMs))
    }

    override fun onGetAliveDeviceFailed(device: Device, throwable: Throwable, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("get-alive-device", "failure"), durationMs))
    }

    override fun onClearPackageSuccess(device: Device, attempt: Int, name: String, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("clear-package", "success"), durationMs))
    }

    override fun onClearPackageError(
        device: Device,
        attempt: Int,
        name: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("clear-package", "error"), durationMs))
    }

    override fun onClearPackageFailure(device: Device, name: String, throwable: Throwable, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("clear-package", "failure"), durationMs))
    }

    override fun onPullSuccess(device: Device, from: Path, to: Path, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("pull", "success"), durationMs))
    }

    override fun onPullError(device: Device, attempt: Int, from: Path, throwable: Throwable, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("pull", "error"), durationMs))
    }

    override fun onPullFailure(device: Device, from: Path, throwable: Throwable, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("pull", "failure"), durationMs))
    }

    override fun onClearDirectorySuccess(device: Device, remotePath: Path, output: String, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("clear-directory", "success"), durationMs))
    }

    override fun onClearDirectoryError(
        device: Device,
        attempt: Int,
        remotePath: Path,
        throwable: Throwable,
        durationMs: Long
    ) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("clear-directory", "error"), durationMs))
    }

    override fun onClearDirectoryFailure(device: Device, remotePath: Path, throwable: Throwable, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("clear-directory", "failure"), durationMs))
    }

    override fun onListSuccess(device: Device, remotePath: String, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("list", "success"), durationMs))
    }

    override fun onListError(device: Device, attempt: Int, remotePath: String, throwable: Throwable, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("list", "error"), durationMs))
    }

    override fun onListFailure(device: Device, remotePath: String, throwable: Throwable, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("list", "failure"), durationMs))
    }

    override fun onRunTestPassed(device: Device, testName: String, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("run-test", "passed"), durationMs))
    }

    override fun onRunTestIgnored(device: Device, testName: String, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("run-test", "ignored"), durationMs))
    }

    override fun onRunTestRunError(device: Device, testName: String, errorMessage: String, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("run-test", "error"), durationMs))
    }

    override fun onRunTestInfrastructureError(
        device: Device,
        testName: String,
        errorMessage: String,
        throwable: Throwable?,
        durationMs: Long
    ) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("run-test", "infrastructure-error"), durationMs))
    }

    override fun onRunTestFailedOnStart(device: Device, message: String, durationMs: Long) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("run-test", "failed-on-start"), durationMs))
    }

    override fun onRunTestFailedOnInstrumentationParse(
        device: Device,
        message: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        statsDSender.send(TimeMetric(prefixWithDevice(device).append("run-test", "failed-instrum-parse"), durationMs))
    }

    private fun prefixWithDevice(device: Device): SeriesName {
        return prefix.append(device.api.toString())
    }
}
