package com.avito.emcee.worker.internal.consumer

import com.avito.android.Problem
import com.avito.android.Result
import com.avito.android.asRuntimeException
import com.avito.emcee.queue.BucketResult
import com.avito.emcee.queue.BucketResultContainer
import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.DeviceConfiguration
import com.avito.emcee.queue.Payload
import com.avito.emcee.queue.TestConfigurationContainer
import com.avito.emcee.queue.TestEntry
import com.avito.emcee.queue.TestExecutionBehavior
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
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

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

                val startTime = System.currentTimeMillis()
                val results: List<TestExecutor.Result>
                val executionTime = measureTime {
                    results = executeTests(
                        payload = testConfigurationContainer.payload,
                        testEntries = testEntries,
                        testExecutionBehavior = testExecutionBehavior,
                    )
                }
                sendTestResults(job, results, startTime, executionTime)

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

    private suspend fun executeTests(
        payload: TestConfigurationContainer.Payload,
        testEntries: List<TestEntry>,
        testExecutionBehavior: TestExecutionBehavior
    ): List<TestExecutor.Result> = with(payload) {
        val executor = deviceProvider.provide(
            DeviceConfiguration(
                deviceType,
                sdkVersion
            )
        )
        executor.beforeTestBucket()
        val applicationPackage = androidBuildArtifacts.app.packageName
        val testPackage = androidBuildArtifacts.testApp.packageName
        val instrumentationRunnerClass = androidBuildArtifacts.runnerClass
        val (apk, testApk) = downloadArtifacts(androidBuildArtifacts)
        val results = testEntries.map { testEntry ->
            executor.execute(
                TestExecutor.Job(
                    apk = apk,
                    testApk = testApk,
                    test = testEntry,
                    applicationPackage = applicationPackage,
                    testPackage = testPackage,
                    instrumentationRunnerClass = instrumentationRunnerClass,
                    testExecutionBehavior = testExecutionBehavior,
                    testMaximumDuration = testMaximumDuration
                )
            )
        }
        executor.afterTestBucket()
        return results
    }

    private suspend fun sendTestResults(
        job: TestJobProducer.Job,
        results: List<TestExecutor.Result>,
        startTime: Long,
        executionTime: Duration
    ) {
        var result: Result<Unit>
        var tries = 0
        do {
            result = api.sendBucketResult(
                createBucketResult(
                    workerId = job.workerId,
                    bucketId = job.bucket.bucketId,
                    signature = job.payloadSignature,
                    startTime = startTime,
                    executionTime = executionTime,
                    payload = job.bucket.payloadContainer.payload,
                    results = results,
                )
            )
            tries++
        } while (result.isFailure() && tries <= 3) // TODO: move tries count to config file and OkHttp interceptor

        result.onFailure { throwable ->
            val problem = Problem.Builder(
                shortDescription = "Failed to send test results.",
                context = "Sending test results to the queue after test execution.",
            ).apply {
                addSolution("Check if result json is valid. Queue may not accept the test result.")
                addSolution("Check if queue is running and available.")
                throwable(throwable)
            }.build()
            throw problem.asRuntimeException()
        }
    }

    private fun createBucketResult(
        workerId: String,
        bucketId: String,
        signature: PayloadSignature,
        startTime: Long,
        executionTime: Duration,
        payload: Payload,
        results: List<TestExecutor.Result>
    ) = SendBucketResultBody(
        bucketId = bucketId,
        payloadSignature = signature,
        workerId = workerId,
        bucketResultContainer = BucketResultContainer(
            payload = BucketResult(
                device = DeviceConfiguration(
                    type = payload.testConfigurationContainer.payload.deviceType,
                    sdkVersion = payload.testConfigurationContainer.payload.sdkVersion
                ),
                unfilteredResults = payload.testEntries.map { entry ->
                    BucketResult.UnfilteredResult(
                        testEntry = entry,
                        testRunResults = listOf(
                            BucketResult.UnfilteredResult.TestRunResult(
                                uuid = UUID.randomUUID().toString(),
                                duration = executionTime,
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
    )
}
