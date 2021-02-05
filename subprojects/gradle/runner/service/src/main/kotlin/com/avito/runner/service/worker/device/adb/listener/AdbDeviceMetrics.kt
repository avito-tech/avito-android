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

    override fun getSdkPropertySuccess(attempt: Int, api: Int) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.success"))
    }

    override fun getSdkPropertyAttemptFail(attempt: Int) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.attempt-fail"))
    }

    override fun getSdkPropertyFailure(throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("get-sdk-property.failure"))
    }

    override fun installApplicationSuccess(attempt: Int, applicationPackage: String) {
        statsDSender.send(prefix, CountMetric("install-application.success"))
    }

    override fun installApplicationAttemptFail(attempt: Int, applicationPackage: String) {
        statsDSender.send(prefix, CountMetric("install-application.attempt-fail"))
    }

    override fun installApplicationFailure(applicationPackage: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("install-application.failure"))
    }

    override fun getAliveDeviceSuccess(attempt: Int) {
        statsDSender.send(prefix, CountMetric("get-alive-device.success"))
    }

    override fun getAliveDeviceAttemptFail(attempt: Int) {
        statsDSender.send(prefix, CountMetric("get-alive-device.attempt-fail"))
    }

    override fun getAliveDeviceFailed(throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("get-alive-device.failure"))
    }

    override fun clearPackageSuccess(attempt: Int, name: String) {
        statsDSender.send(prefix, CountMetric("clear-package.success"))
    }

    override fun clearPackageAttemptFail(attempt: Int, name: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("clear-package.attempt-fail"))
    }

    override fun clearPackageFailure(name: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("clear-package.failure"))
    }

    override fun pullSuccess(from: Path, to: Path) {
        statsDSender.send(prefix, CountMetric("pull.success"))
    }

    override fun pullAttemptFail(attempt: Int, from: Path, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("pull.attempt-fail"))
    }

    override fun pullFailure(from: Path, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("pull.failure"))
    }

    override fun clearDirectorySuccess(remotePath: Path, output: String) {
        statsDSender.send(prefix, CountMetric("clear-directory.success"))
    }

    override fun clearDirectoryNothingDone(remotePath: Path) {
        statsDSender.send(prefix, CountMetric("clear-directory.nothing"))
    }

    override fun clearDirectoryAttemptFail(attempt: Int, remotePath: Path, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("clear-directory.attempt-fail"))
    }

    override fun clearDirectoryFailure(remotePath: Path, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("clear-directory.failure"))
    }

    override fun listSuccess(remotePath: String) {
        statsDSender.send(prefix, CountMetric("list.success"))
    }

    override fun listAttemptFail(attempt: Int, remotePath: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("list.attempt-fail"))
    }

    override fun listFailure(remotePath: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("list.failure"))
    }

    override fun runTestPassed(testName: String) {
        statsDSender.send(prefix, CountMetric("run-test.passed"))
    }

    override fun runTestIgnored(testName: String) {
        statsDSender.send(prefix, CountMetric("run-test.ignored"))
    }

    override fun runTestRunError(testName: String, errorMessage: String) {
        statsDSender.send(prefix, CountMetric("run-test.error"))
    }

    override fun runTestInfrastructureError(testName: String, errorMessage: String, throwable: Throwable?) {
        statsDSender.send(prefix, CountMetric("run-test.infrastructure-error"))
    }

    override fun runTestFailedOnStart(message: String) {
        statsDSender.send(prefix, CountMetric("run-test.failed-on-start"))
    }

    override fun runTestFailedOnInstrumentationParse(message: String, throwable: Throwable) {
        statsDSender.send(prefix, CountMetric("run-test.failed-instrum-parse"))
    }
}
