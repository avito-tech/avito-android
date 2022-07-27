package com.avito.emcee.worker.internal.consumer

import com.avito.emcee.queue.DeviceConfiguration
import com.avito.emcee.queue.Payload
import com.avito.emcee.worker.SendBucketResultBody
import com.avito.emcee.worker.SendBucketResultBody.BucketResult.UnfilteredResult.TestRunResult.StartTime
import com.avito.emcee.worker.WorkerQueueApi
import com.avito.emcee.worker.internal.TestExecutor
import com.avito.emcee.worker.internal.TestJobProducer
import com.avito.emcee.worker.internal.storage.ProcessingBucketsStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class FakeTestJobConsumer(
    private val api: WorkerQueueApi,
    private val bucketsStorage: ProcessingBucketsStorage,
) : TestJobConsumer {

    override fun consume(jobs: Flow<TestJobProducer.Job>): Flow<TestJobConsumer.Result> = jobs.map { job ->
        bucketsStorage.add(job.bucket)

        val startTime = StartTime(System.currentTimeMillis())
        delay(5.seconds)

        api.sendBucketResult(
            createBucketResult(
                workerId = job.workerId,
                bucketId = job.bucket.bucketId,
                signature = job.payloadSignature,
                startTime = startTime,
                payload = job.bucket.payloadContainer.payload
            )
        )

        bucketsStorage.remove(job.bucket)

        val results = job.bucket.payloadContainer.payload.testEntries.map { TestExecutor.Result(true) }
        TestJobConsumer.Result(results)
    }

    private fun createBucketResult(
        workerId: String,
        bucketId: String,
        signature: String,
        startTime: StartTime,
        payload: Payload,
    ) = SendBucketResultBody(
        bucketId = bucketId,
        payloadSignature = signature,
        workerId = workerId,
        bucketResult = SendBucketResultBody.BucketResult(
            device = DeviceConfiguration(
                type = payload.testConfiguration.deviceType,
                sdkVersion = payload.testConfiguration.sdkVersion
            ),
            unfilteredResults = payload.testEntries.map {
                SendBucketResultBody.BucketResult.UnfilteredResult(
                    testEntry = it,
                    testRunResults = listOf(
                        SendBucketResultBody.BucketResult.UnfilteredResult.TestRunResult(
                            udid = UUID.randomUUID().toString(),
                            duration = 5,
                            exceptions = emptyList(),
                            hostName = "",
                            logs = emptyList(),
                            startTime = startTime,
                            succeeded = true
                        )
                    )
                )
            }
        )
    )
}
