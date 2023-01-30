package com.avito.emcee.client.internal

import com.avito.emcee.queue.ApkLocation
import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.DeviceConfiguration
import com.avito.emcee.queue.RemoteApk
import com.avito.emcee.queue.TestConfigurationContainer
import okhttp3.HttpUrl
import kotlin.time.Duration

internal class TestConfigurationContainerFactory(
    private val apkUrl: HttpUrl,
    private val testApkUrl: HttpUrl,
    private val testMaximumDuration: Duration,
    private val appPackage: String,
    private val testAppPackage: String,
    private val testRunnerClass: String,
) {
    fun create(device: DeviceConfiguration) =
        TestConfigurationContainer(
            TestConfigurationContainer.Payload(
                androidBuildArtifacts = BuildArtifacts(
                    app = RemoteApk(ApkLocation(apkUrl.toString()), appPackage),
                    testApp = RemoteApk(ApkLocation(testApkUrl.toString()), testAppPackage),
                    runnerClass = testRunnerClass
                ),
                deviceType = device.type,
                sdkVersion = device.sdkVersion,
                testMaximumDuration = testMaximumDuration,
            )
        )
}
