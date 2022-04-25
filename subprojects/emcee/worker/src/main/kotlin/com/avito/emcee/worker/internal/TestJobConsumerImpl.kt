package com.avito.emcee.worker.internal

import com.avito.emcee.queue.BuildArtifacts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class TestJobConsumerImpl(
    private val deviceProvider: TestExecutorProvider
) : TestJobConsumer {

    override fun consume(
        jobs: Flow<TestJobProducer.Job>
    ): Flow<TestJobConsumer.Result> {
        return jobs.map { job ->
            with(job.bucket) {
                val executor = deviceProvider.provide(payload.device)
                executor.beforeTestBucket()
                val applicationPackage = "stub"
                val testPackage = "stub"
                val instrumentationRunnerClass = "stub"
                val (apk, testApk) = downloadArtifacts(payload.buildMetadata.artifacts)
                val results = payload.testEntries.map { testEntry ->
                    executor.execute(
                        TestExecutor.Job(
                            apk = apk,
                            testApk = testApk,
                            test = testEntry,
                            applicationPackage = applicationPackage,
                            testPackage = testPackage,
                            instrumentationRunnerClass = instrumentationRunnerClass,
                            testExecutionBehavior = payload.testExecutionBehavior,
                            testMaximumDuration = Duration.seconds(
                                payload.testTimeoutConfiguration.testMaximumDurationSec
                            )
                        )
                    )
                }
                executor.afterTestBucket()
                TestJobConsumer.Result(results)
            }
        }
    }

    private suspend fun downloadArtifacts(buildArtifacts: BuildArtifacts): Pair<File, File> {
        TODO("Implement and use $buildArtifacts")
    }
}
