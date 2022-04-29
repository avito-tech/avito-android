package com.avito.emcee.internal

import com.avito.emcee.queue.ApkLocation
import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.DeviceConfiguration
import com.avito.emcee.queue.RemoteApk
import com.avito.emcee.queue.TestConfiguration
import com.avito.emcee.queue.TestExecutionBehavior
import okhttp3.HttpUrl

internal class TestConfigurationFactory(
    private val apkUrl: HttpUrl,
    private val testApkUrl: HttpUrl,
    private val testMaximumDurationSec: Long,
    private val testExecutionBehavior: TestExecutionBehavior,
    private val apkPackage: String = "", // TODO: parse app package name
    private val testAppPackage: String = "", // TODO: parse test app package name
    private val testRunnerClass: String = "", // TODO: provide test runner class
) {
    fun create(device: DeviceConfiguration) =
        TestConfiguration(
            buildArtifacts = BuildArtifacts(
                app = RemoteApk(ApkLocation(apkUrl.toString()), apkPackage),
                testApp = RemoteApk(ApkLocation(testApkUrl.toString()), testAppPackage),
                runnerClass = testRunnerClass
            ),
            deviceType = device.type,
            sdkVersion = device.sdkVersion,
            testMaximumDurationSec = testMaximumDurationSec,
            testExecutionBehavior = testExecutionBehavior
        )
}
