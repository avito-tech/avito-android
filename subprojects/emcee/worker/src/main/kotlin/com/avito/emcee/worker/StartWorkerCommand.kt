package com.avito.emcee.worker

import com.avito.emcee.worker.WorkerQueueApi.Companion.create
import com.avito.emcee.worker.internal.SingleInstanceAndroidDeviceTestExecutorProvider
import com.avito.emcee.worker.internal.TestJobConsumerImpl
import com.avito.emcee.worker.internal.TestJobProducerImpl
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCli
internal class StartWorkerCommand(
    name: String,
    description: String
) : Subcommand(name, description) {

    private val workerId: String by option(
        type = ArgType.String,
        description = "Helps Queue to identify worker's interactions"
    ).required()

    private val restAddress: String by option(
        type = ArgType.String,
        description = "Helps Queue to interact with worker"
    ).required()

    private val queueUrl: String by option(
        type = ArgType.String,
        description = "Url where worker will ask for test buckets"
    ).required()

    override fun execute() {
        val producer = TestJobProducerImpl(
            api = Retrofit.Builder().create(queueUrl),
            workerId = workerId,
            restAddress = restAddress
        )
        val consumer = TestJobConsumerImpl(SingleInstanceAndroidDeviceTestExecutorProvider())
        runBlocking {
            consumer.consume(
                producer.getJobs()
            ).collect { result ->
                println(result)
            }
        }
    }
}
