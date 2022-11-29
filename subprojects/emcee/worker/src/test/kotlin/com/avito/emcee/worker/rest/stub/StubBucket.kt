package com.avito.emcee.worker.rest.stub

import com.avito.emcee.queue.ApkLocation
import com.avito.emcee.queue.Bucket
import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.Payload
import com.avito.emcee.queue.PayloadContainer
import com.avito.emcee.queue.RemoteApk
import com.avito.emcee.queue.TestConfiguration
import com.avito.emcee.queue.TestExecutionBehavior

fun Bucket.Companion.stub(bucketId: String) = Bucket(
    bucketId = bucketId,
    derivedFrom = null,
    payloadContainer = PayloadContainer(
        payload = Payload(
            testEntries = emptyList(),
            testConfiguration = TestConfiguration(
                buildArtifacts = BuildArtifacts(
                    app = RemoteApk(
                        location = ApkLocation(url = "http://stub/stub.apk"),
                        packageName = "com.avito.android"
                    ),
                    testApp = RemoteApk(
                        location = ApkLocation(url = "http://stub/stub-test.apk"),
                        packageName = "com.avito.android.test"
                    ),
                    runnerClass = "com.avito.runner.TestRunner"
                ),
                deviceType = "Android",
                sdkVersion = 30,
                testExecutionBehavior = TestExecutionBehavior(
                    environment = emptyMap(),
                    retries = 3
                ),
                testMaximumDurationSec = 300
            ),
        ),
        payloadType = "stub"
    ),
    analyticsConfiguration = Any(),
    workerCapabilityRequirements = emptyList()
)
