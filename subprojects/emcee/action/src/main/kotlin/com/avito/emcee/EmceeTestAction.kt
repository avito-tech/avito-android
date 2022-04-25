package com.avito.emcee

import com.avito.emcee.internal.FileUploader
import com.avito.emcee.internal.TestsParser
import com.avito.emcee.queue.BuildArtifact
import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.JobStatusBody
import com.avito.emcee.queue.QueueApi
import com.avito.emcee.queue.ScheduleTestsBody
import com.avito.emcee.queue.TestRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

public class EmceeTestAction internal constructor(
    private val queueApi: QueueApi,
    private val uploader: FileUploader,
    private val testsParser: TestsParser,
) {

    @ExperimentalTime
    public fun execute(config: EmceeTestActionConfig) {
        runBlocking {
            val tests: List<TestRequest> = createTestRequests(config)
            queueApi.scheduleTests(
                ScheduleTestsBody(
                    prioritizedJob = config.job,
                    scheduleStrategy = config.scheduleStrategy,
                    tests = tests
                )
            )
            do {
                // TODO handle error
                val status = queueApi.jobStatus(JobStatusBody(id = config.job.id))
                delay(Duration.seconds(10)) // TODO how often? to config
            } while (status.queueState.dequeuedBucketCount + status.queueState.enqueuedBucketCount != 0)
        }
    }

    private suspend fun createTestRequests(
        config: EmceeTestActionConfig,
    ): List<TestRequest> {
        return withContext(Dispatchers.IO) {
            val apkUrl = async { uploader.upload(config.apk) }
            val testApkUrl = async { uploader.upload(config.testApk) }
            val testsInApk = async { testsParser.parse(config.testApk) }
            val apkPackage = "" // TODO: parse app package name
            val testAppPackage = "" // TODO: parse test app package name
            testsInApk.await().getOrThrow()
                .flatMap { testEntry ->
                    config.devices.map { device ->
                        TestRequest(
                            buildArtifacts = BuildArtifacts(
                                app = BuildArtifact(apkUrl.await().toString(), apkPackage),
                                testApp = BuildArtifact(testApkUrl.await().toString(), testAppPackage)
                            ),
                            device = device,
                            testEntry = testEntry,
                            executionBehavior = config.testExecutionBehavior,
                            testMaximumDurationSec = config.testMaximumDurationSec,
                            analyticsConfiguration = Any()
                        )
                    }
                }
        }
    }
}
