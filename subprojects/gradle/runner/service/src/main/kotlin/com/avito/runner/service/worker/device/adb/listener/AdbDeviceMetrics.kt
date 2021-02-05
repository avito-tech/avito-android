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

    override fun onGetSdkPropertyAttemptFail(attempt: Int) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.attempt-fail"))
    }

    override fun onGetSdkPropertyFailure(throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.failure"))
    }

    override fun onInstallApplicationSuccess(device: Device, attempt: Int, applicationPackage: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("install-application.success"))
    }

    override fun onInstallApplicationAttemptFail(device: Device, attempt: Int, applicationPackage: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("install-application.attempt-fail"))
    }

    override fun onInstallApplicationFailure(device: Device, applicationPackage: String, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("install-application.failure"))
    }

    override fun onGetAliveDeviceSuccess(device: Device, attempt: Int) {
        statsDSender.send(prefixWithDevice(device), CountMetric("get-alive-device.success"))
    }

    override fun onGetAliveDeviceAttemptFail(device: Device, attempt: Int) {
        statsDSender.send(prefixWithDevice(device), CountMetric("get-alive-device.attempt-fail"))
    }

    override fun onGetAliveDeviceFailed(device: Device, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("get-alive-device.failure"))
    }

    override fun onClearPackageSuccess(device: Device, attempt: Int, name: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-package.success"))
    }

    override fun onClearPackageAttemptFail(device: Device, attempt: Int, name: String, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-package.attempt-fail"))
    }

    override fun onClearPackageFailure(device: Device, name: String, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-package.failure"))
    }

    override fun onPullSuccess(device: Device, from: Path, to: Path) {
        statsDSender.send(prefixWithDevice(device), CountMetric("pull.success"))
    }

    override fun onPullAttemptFail(device: Device, attempt: Int, from: Path, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("pull.attempt-fail"))
    }

    override fun onPullFailure(device: Device, from: Path, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("pull.failure"))
    }

    override fun onClearDirectorySuccess(device: Device, remotePath: Path, output: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-directory.success"))
    }

    override fun onClearDirectoryNothingDone(device: Device, remotePath: Path) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-directory.nothing"))
    }

    override fun onClearDirectoryAttemptFail(device: Device, attempt: Int, remotePath: Path, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-directory.attempt-fail"))
    }

    override fun onClearDirectoryFailure(device: Device, remotePath: Path, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("clear-directory.failure"))
    }

    override fun onListSuccess(device: Device, remotePath: String) {
        statsDSender.send(prefixWithDevice(device), CountMetric("list.success"))
    }

    override fun onListAttemptFail(device: Device, attempt: Int, remotePath: String, throwable: Throwable) {
        statsDSender.send(prefixWithDevice(device), CountMetric("list.attempt-fail"))
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
