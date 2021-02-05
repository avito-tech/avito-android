package com.avito.runner.service.worker.device.adb.listener

import com.avito.android.stats.CountMetric
import com.avito.android.stats.StatsDSender
import java.nio.file.Path

class AdbDeviceMetrics(
    private val statsDSender: StatsDSender,
    buildId: String,
    instrumentationConfigName: String
) : AdbDeviceEventsListener {

    private val prefix = "testrunner.$buildId.$instrumentationConfigName.adb"

    override fun onGetSdkPropertySuccess(attempt: Int, api: Int) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.success"))
    }

    override fun onGetSdkPropertyAttemptFail(attempt: Int) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.attempt-fail"))
    }

    override fun onGetSdkPropertyFailure(throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.failure"))
    }

    override fun onInstallApplicationSuccess(attempt: Int, applicationPackage: String) {
        statsDSender.send(prefix, CountMetric("install-application.success"))
    }

    override fun onInstallApplicationAttemptFail(attempt: Int, applicationPackage: String) {
        statsDSender.send(prefix, CountMetric("install-application.attempt-fail"))
    }

    override fun onInstallApplicationFailure(applicationPackage: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("install-application.failure"))
    }

    override fun onGetAliveDeviceSuccess(attempt: Int) {
        statsDSender.send(prefix, CountMetric("get-alive-device.success"))
    }

    override fun onGetAliveDeviceAttemptFail(attempt: Int) {
        statsDSender.send(prefix, CountMetric("get-alive-device.attempt-fail"))
    }

    override fun onGetAliveDeviceFailed(throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("get-alive-device.failure"))
    }

    override fun onClearPackageSuccess(attempt: Int, name: String) {
        statsDSender.send(prefix, CountMetric("clear-package.success"))
    }

    override fun onClearPackageAttemptFail(attempt: Int, name: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("clear-package.attempt-fail"))
    }

    override fun onClearPackageFailure(name: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("clear-package.failure"))
    }

    override fun onPullSuccess(from: Path, to: Path) {
        statsDSender.send(prefix, CountMetric("pull.success"))
    }

    override fun onPullAttemptFail(attempt: Int, from: Path, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("pull.attempt-fail"))
    }

    override fun onPullFailure(from: Path, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("pull.failure"))
    }

    override fun onClearDirectorySuccess(remotePath: Path, output: String) {
        statsDSender.send(prefix, CountMetric("clear-directory.success"))
    }

    override fun onClearDirectoryNothingDone(remotePath: Path) {
        statsDSender.send(prefix, CountMetric("clear-directory.nothing"))
    }

    override fun onClearDirectoryAttemptFail(attempt: Int, remotePath: Path, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("clear-directory.attempt-fail"))
    }

    override fun onClearDirectoryFailure(remotePath: Path, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("clear-directory.failure"))
    }

    override fun onListSuccess(remotePath: String) {
        statsDSender.send(prefix, CountMetric("list.success"))
    }

    override fun onListAttemptFail(attempt: Int, remotePath: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("list.attempt-fail"))
    }

    override fun onListFailure(remotePath: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("list.failure"))
    }

    override fun onRunTestPassed(testName: String) {
        statsDSender.send(prefix, CountMetric("run-test.passed"))
    }

    override fun onRunTestIgnored(testName: String) {
        statsDSender.send(prefix, CountMetric("run-test.ignored"))
    }

    override fun onRunTestRunError(testName: String, errorMessage: String) {
        statsDSender.send(prefix, CountMetric("run-test.error"))
    }

    override fun onRunTestInfrastructureError(testName: String, errorMessage: String, throwable: Throwable?) {
        statsDSender.send(prefix, CountMetric("run-test.infrastructure-error"))
    }

    override fun onRunTestFailedOnStart(message: String) {
        statsDSender.send(prefix, CountMetric("run-test.failed-on-start"))
    }

    override fun onRunTestFailedOnInstrumentationParse(message: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("run-test.failed-instrum-parse"))
    }
}
