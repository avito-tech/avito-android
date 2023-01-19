package com.avito.emcee.worker.internal.consumer

import com.avito.emcee.queue.BucketResult
import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.DeviceConfiguration
import com.avito.emcee.queue.Payload
import com.avito.emcee.worker.SendBucketResultBody
import com.avito.emcee.worker.WorkerQueueApi
import com.avito.emcee.worker.configuration.PayloadSignature
import com.avito.emcee.worker.internal.DeviceTestExecutorProvider
import com.avito.emcee.worker.internal.TestExecutor
import com.avito.emcee.worker.internal.TestJobProducer
import com.avito.emcee.worker.internal.artifacts.FileDownloader
import com.avito.emcee.worker.internal.storage.ProcessingBucketsStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class RealTestJobConsumer(
    private val api: WorkerQueueApi,
    private val deviceProvider: DeviceTestExecutorProvider,
    private val fileDownloader: FileDownloader,
    private val bucketsStorage: ProcessingBucketsStorage
) : TestJobConsumer {

    override fun consume(
        jobs: Flow<TestJobProducer.Job>
    ): Flow<TestJobConsumer.Result> {
        return jobs.map { job ->
            with(job.bucket.payloadContainer.payload) {
                bucketsStorage.add(job.bucket)

                val startTime = BucketResult.UnfilteredResult.TestRunResult.StartTime(System.currentTimeMillis())
                val results = executeTest()
                sendTestResults(job, results, startTime)

                bucketsStorage.remove(job.bucket)

                TestJobConsumer.Result(results)
            }
        }
    }

    private suspend fun downloadArtifacts(buildArtifacts: BuildArtifacts): Pair<File, File> {
        val apk = fileDownloader.download(buildArtifacts.app.location.url.toHttpUrl())
        val testApk = fileDownloader.download(buildArtifacts.testApp.location.url.toHttpUrl())
        return apk to testApk
    }

    private suspend fun Payload.executeTest(): List<TestExecutor.Result> {
        val executor = deviceProvider.provide(
            DeviceConfiguration(
                testConfiguration.deviceType,
                testConfiguration.sdkVersion
            )
        )
        executor.beforeTestBucket()
        val applicationPackage = testConfiguration.buildArtifacts.app.packageName
        val testPackage = testConfiguration.buildArtifacts.testApp.packageName
        val instrumentationRunnerClass = testConfiguration.buildArtifacts.runnerClass
        val (apk, testApk) = downloadArtifacts(testConfiguration.buildArtifacts)
        val results = testEntries.map { testEntry ->
            executor.execute(
                TestExecutor.Job(
                    apk = apk,
                    testApk = testApk,
                    test = testEntry,
                    applicationPackage = applicationPackage,
                    testPackage = testPackage,
                    instrumentationRunnerClass = instrumentationRunnerClass,
                    testExecutionBehavior = testConfiguration.testExecutionBehavior,
                    testMaximumDuration = testConfiguration.testMaximumDuration
                )
            )
        }
        executor.afterTestBucket()
        return results
    }

    private suspend fun sendTestResults(
        job: TestJobProducer.Job,
        results: List<TestExecutor.Result>,
        startTime: BucketResult.UnfilteredResult.TestRunResult.StartTime
    ) {
        api.sendBucketResult(
            createBucketResult(
                workerId = job.workerId,
                bucketId = job.bucket.bucketId,
                signature = job.payloadSignature,
                startTime = startTime,
                payload = job.bucket.payloadContainer.payload,
                results = results,
            )
        )
    }

    private fun createBucketResult(
        workerId: String,
        bucketId: String,
        signature: PayloadSignature,
        startTime: BucketResult.UnfilteredResult.TestRunResult.StartTime,
        payload: Payload,
        results: List<TestExecutor.Result>
    ) = SendBucketResultBody(
        bucketId = bucketId,
        payloadSignature = signature,
        workerId = workerId,
        bucketResult = BucketResult(
            device = DeviceConfiguration(
                type = payload.testConfiguration.deviceType,
                sdkVersion = payload.testConfiguration.sdkVersion
            ),
            unfilteredResults = payload.testEntries.map { entry ->
                BucketResult.UnfilteredResult(
                    testEntry = entry,
                    testRunResults = listOf(
                        BucketResult.UnfilteredResult.TestRunResult(
                            uuid = UUID.randomUUID().toString(),
                            duration = 5.seconds, // TODO: calculate real execution time
                            exceptions = emptyList(),
                            hostName = "",
                            logs = emptyList(),
                            startTime = startTime,
                            succeeded = results.find { it.testEntry == entry }?.success
                                ?: error("Cannot find $entry in test results: $results")
                        )
                    )
                )
            }
        )
    )
}
