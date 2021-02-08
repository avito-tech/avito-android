package com.avito.runner.service.worker.device.adb.listener

import com.avito.android.stats.CountMetric
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.graphiteSeries
import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

class AdbDeviceMetrics(
    private val statsDSender: StatsDSender,
    runnerPrefix: String
) : AdbDeviceEventsListener {

    private val prefix = graphiteSeries(runnerPrefix, "adb")

    override fun onGetSdkPropertySuccess(attempt: Int, api: Int) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.success"))
    }

    override fun onGetSdkPropertyError(attempt: Int) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.error"))
    }

    override fun onGetSdkPropertyFailure(throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.failure"))
    }

    override fun onInstallApplicationSuccess(device: Device, attempt: Int, applicationPackage: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("install-application.success"))
    }

    override fun onInstallApplicationError(device: Device, attempt: Int, applicationPackage: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("install-application.error"))
    }

    override fun onInstallApplicationFailure(device: Device, applicationPackage: String, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("install-application.failure"))
    }

    override fun onGetAliveDeviceSuccess(device: Device, attempt: Int) {
        statsDSender.send(prefixWithDevice(device), CountMetric("get-alive-device.success"))
    }

    override fun onGetAliveDeviceError(device: Device, attempt: Int) {
        statsDSender.send(prefixWithDevice(device), CountMetric("get-alive-device.error"))
    }

    override fun onGetAliveDeviceFailed(device: Device, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("get-alive-device.failure"))
    }

    override fun onClearPackageSuccess(device: Device, attempt: Int, name: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-package.success"))
    }

    override fun onClearPackageError(device: Device, attempt: Int, name: String, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-package.error"))
    }

    override fun onClearPackageFailure(device: Device, name: String, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-package.failure"))
    }

    override fun onPullSuccess(device: Device, from: Path, to: Path) {
        statsDSender.send(prefixWithDevice(device), CountMetric("pull.success"))
    }

    override fun onPullError(device: Device, attempt: Int, from: Path, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("pull.error"))
    }

    override fun onPullFailure(device: Device, from: Path, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("pull.failure"))
    }

    override fun onClearDirectorySuccess(device: Device, remotePath: Path, output: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-directory.success"))
    }

    override fun onClearDirectoryError(device: Device, attempt: Int, remotePath: Path, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-directory.error"))
    }

    override fun onClearDirectoryFailure(device: Device, remotePath: Path, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-directory.failure"))
    }

    override fun onListSuccess(device: Device, remotePath: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("list.success"))
    }

    override fun onListError(device: Device, attempt: Int, remotePath: String, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("list.error"))
    }

    override fun onListFailure(device: Device, remotePath: String, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("list.failure"))
    }

    override fun onRunTestPassed(device: Device, testName: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("run-test.passed"))
    }

    override fun onRunTestIgnored(device: Device, testName: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("run-test.ignored"))
    }

    override fun onRunTestRunError(device: Device, testName: String, errorMessage: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("run-test.error"))
    }

    override fun onRunTestInfrastructureError(
        device: Device,
        testName: String,
        errorMessage: String,
        throwable: Throwable?
    ) {
        statsDSender.send(prefixWithDevice(device), CountMetric("run-test.infrastructure-error"))
    }

    override fun onRunTestFailedOnStart(device: Device, message: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("run-test.failed-on-start"))
    }

    override fun onRunTestFailedOnInstrumentationParse(device: Device, message: String, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("run-test.failed-instrum-parse"))
    }

    private fun prefixWithDevice(device: Device): String {
        return graphiteSeries(prefix, device.api.toString())
    }
}
