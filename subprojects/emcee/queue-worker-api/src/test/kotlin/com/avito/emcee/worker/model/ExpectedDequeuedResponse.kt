package com.avito.emcee.worker.model

import com.avito.emcee.queue.ApkDescription
import com.avito.emcee.queue.ApkLocation
import com.avito.emcee.queue.Bucket
import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.Payload
import com.avito.emcee.queue.PayloadContainer
import com.avito.emcee.queue.TestConfiguration
import com.avito.emcee.queue.TestEntry
import com.avito.emcee.queue.TestExecutionBehavior
import com.avito.emcee.queue.TestName
import com.avito.emcee.worker.GetBucketResponse

internal fun GetBucketResponse.Companion.dequeued(): GetBucketResponse.Dequeued = GetBucketResponse.Dequeued(
    bucket = Bucket(
        bucketId = "1F10555C-0D48-436F-B0A6-4D0ABF813493",
        payloadContainer = PayloadContainer(
            payload = Payload(
                testEntries = listOf(
                    TestEntry(
                        caseId = null,
                        tags = emptyList(),
                        name = TestName(
                            className = "SomeClassNameWithTests",
                            methodName = "testMethod"
                        )
                    ),
                    TestEntry(
                        caseId = null,
                        tags = emptyList(),
                        name = TestName(
                            className = "AnotherClass",
                            methodName = "test"
                        )
                    )
                ),
                testConfiguration = TestConfiguration(
                    buildArtifacts = BuildArtifacts(
                        app = ApkDescription(
                            location = ApkLocation(url = "https://example.com/artifactory/repo/path/app.apk"),
                            apkPackage = "com.avito.android"
                        ),
                        testApp = ApkDescription(
                            location = ApkLocation("https://example.com/artifactory/repo/path/test.apk"),
                            apkPackage = "com.avito.android.test"
                        ),
                        runnerClass = "com.avito.android.InstrumentationRunner"
                    ),
                    deviceType = "Nexus 5",
                    sdkVersion = 30,
                    testExecutionBehavior = TestExecutionBehavior(
                        environment = mapOf("SOME" to "env values"),
                        retries = 5
                    ),
                    testMaximumDuration = 30.0
                )
            )
        )
    )
)

internal fun GetBucketResponse.Companion.noBucket() = GetBucketResponse.NoBucket(checkAfter = 30)
