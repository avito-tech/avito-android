package com.avito.emcee.worker.model

import com.avito.emcee.queue.Bucket
import com.avito.emcee.queue.BuildArtifact
import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.BuildMetadata
import com.avito.emcee.queue.DeviceConfiguration
import com.avito.emcee.queue.Payload
import com.avito.emcee.queue.TestEntry
import com.avito.emcee.queue.TestExecutionBehavior
import com.avito.emcee.queue.TestName
import com.avito.emcee.queue.TestTimeoutConfiguration
import com.avito.emcee.worker.GetBucketResponse

internal fun GetBucketResponse.Companion.dequeued(): GetBucketResponse.Dequeued = GetBucketResponse.Dequeued(
    bucket = Bucket(
        bucketId = "1F10555C-0D48-436F-B0A6-4D0ABF813493",
        payload = Payload(
            buildMetadata = BuildMetadata(
                artifacts = BuildArtifacts(
                    app = BuildArtifact(
                        apkPath = "/path/to/apk.apk",
                        apkPackage = "com.avito.android"
                    ),
                    testApp = BuildArtifact(
                        apkPath = "/path/to/testApk.apk",
                        apkPackage = "com.avito.android.test"
                    )
                ),
                runnerClass = "com.avito.runner.InstrumentationRunner"
            ),
            device = DeviceConfiguration(
                type = "device",
                sdkVersion = 29,
            ),
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
            testExecutionBehavior = TestExecutionBehavior(
                environment = emptyMap(),
                retries = 5
            ),
            testTimeoutConfiguration = TestTimeoutConfiguration(
                testMaximumDurationSec = 180,
                runnerMaximumDurationSec = 60
            )
        )
    )
)

internal fun GetBucketResponse.Companion.noBucket() = GetBucketResponse.NoBucket(checkAfter = 30)
