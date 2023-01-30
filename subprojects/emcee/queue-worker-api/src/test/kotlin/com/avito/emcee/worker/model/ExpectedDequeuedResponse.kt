package com.avito.emcee.worker.model

import com.avito.emcee.queue.ApkLocation
import com.avito.emcee.queue.Bucket
import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.Payload
import com.avito.emcee.queue.PayloadContainer
import com.avito.emcee.queue.RemoteApk
import com.avito.emcee.queue.TestConfigurationContainer
import com.avito.emcee.queue.TestEntry
import com.avito.emcee.queue.TestExecutionBehavior
import com.avito.emcee.queue.TestName
import com.avito.emcee.worker.GetBucketResponse
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal fun GetBucketResponse.Companion.dequeued(): GetBucketResponse.Dequeued = GetBucketResponse.Dequeued(
    caseId = "bucketDequeued",
    bucket = Bucket(
        bucketId = "BucketFixturesFixedBucketId",
        payloadContainer = PayloadContainer(
            payload = Payload(
                testEntries = listOf(
                    TestEntry(
                        caseId = null,
                        tags = listOf("tag"),
                        name = TestName(
                            className = "class",
                            methodName = "test"
                        )
                    ),
                ),
                testConfigurationContainer = TestConfigurationContainer(
                    payload = TestConfigurationContainer.Payload(
                        androidBuildArtifacts = BuildArtifacts(
                            app = RemoteApk(
                                location = ApkLocation(url = "app.apk"),
                                packageName = "ru.avito.app.package"
                            ),
                            testApp = RemoteApk(
                                location = ApkLocation("tests.apk"),
                                packageName = "ru.avito.tests.package"
                            ),
                            runnerClass = "ru.avito.RunnerClass"
                        ),
                        deviceType = "deviceType",
                        sdkVersion = 23,
                        testMaximumDuration = 1.minutes
                    )
                ),
                testExecutionBehavior = TestExecutionBehavior(
                    numberOfRetries = 0,
                    environment = mapOf("some" to "env"),
                    testMaximumDuration = 1.minutes
                )
            ),
        ),
    )
)

internal fun GetBucketResponse.Companion.noBucket() =
    GetBucketResponse.NoBucket(caseId = "checkAgainLater", checkAfter = 30.seconds)
