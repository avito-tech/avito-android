package com.avito.runner.service.worker.device.adb.listener

import com.avito.android.stats.CountMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

class AdbDeviceMetrics(
    private val statsDSender: StatsDSender,
    runnerPrefix: SeriesName
) : AdbDeviceEventsListener {

    private val prefix = runnerPrefix.append("adb")

    override fun onGetSdkPropertySuccess(attempt: Int, api: Int) {
        statsDSender.send(CountMetric(prefix.append("get-sdk-property", "success")))
    }

    override fun onGetSdkPropertyError(attempt: Int) {
        statsDSender.send(CountMetric(prefix.append("get-sdk-property", "error")))
    }

    override fun onGetSdkPropertyFailure(throwable: Throwable) {
        statsDSender.send(CountMetric(prefix.append("get-sdk-property", "failure")))
    }

    override fun onInstallApplicationSuccess(device: Device, attempt: Int, applicationPackage: String) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("install-application", "success")))
    }

    override fun onInstallApplicationError(device: Device, attempt: Int, applicationPackage: String) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("install-application", "error")))
    }

    override fun onInstallApplicationFailure(device: Device, applicationPackage: String, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("install-application", "failure")))
    }

    override fun onGetAliveDeviceSuccess(device: Device, attempt: Int) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("get-alive-device", "success")))
    }

    override fun onGetAliveDeviceError(device: Device, attempt: Int) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("get-alive-device", "error")))
    }

    override fun onGetAliveDeviceFailed(device: Device, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("get-alive-device", "failure")))
    }

    override fun onClearPackageSuccess(device: Device, attempt: Int, name: String) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("clear-package", "success")))
    }

    override fun onClearPackageError(device: Device, attempt: Int, name: String, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("clear-package", "error")))
    }

    override fun onClearPackageFailure(device: Device, name: String, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("clear-package", "failure")))
    }

    override fun onPullSuccess(device: Device, from: Path, to: Path) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("pull", "success")))
    }

    override fun onPullError(device: Device, attempt: Int, from: Path, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("pull", "error")))
    }

    override fun onPullFailure(device: Device, from: Path, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("pull", "failure")))
    }

    override fun onClearDirectorySuccess(device: Device, remotePath: Path, output: String) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("clear-directory", "success")))
    }

    override fun onClearDirectoryError(device: Device, attempt: Int, remotePath: Path, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("clear-directory", "error")))
    }

    override fun onClearDirectoryFailure(device: Device, remotePath: Path, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("clear-directory", "failure")))
    }

    override fun onListSuccess(device: Device, remotePath: String) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("list", "success")))
    }

    override fun onListError(device: Device, attempt: Int, remotePath: String, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("list", "error")))
    }

    override fun onListFailure(device: Device, remotePath: String, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("list", "failure")))
    }

    override fun onRunTestPassed(device: Device, testName: String) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("run-test", "passed")))
    }

    override fun onRunTestIgnored(device: Device, testName: String) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("run-test", "ignored")))
    }

    override fun onRunTestRunError(device: Device, testName: String, errorMessage: String) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("run-test", "error")))
    }

    override fun onRunTestInfrastructureError(
        device: Device,
        testName: String,
        errorMessage: String,
        throwable: Throwable?
    ) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("run-test", "infrastructure-error")))
    }

    override fun onRunTestFailedOnStart(device: Device, message: String) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("run-test", "failed-on-start")))
    }

    override fun onRunTestFailedOnInstrumentationParse(device: Device, message: String, throwable: Throwable) {
        statsDSender.send(CountMetric(prefixWithDevice(device).append("run-test", "failed-instrum-parse")))
    }

    private fun prefixWithDevice(device: Device): SeriesName {
        return prefix.append(device.api.toString())
    }
}
