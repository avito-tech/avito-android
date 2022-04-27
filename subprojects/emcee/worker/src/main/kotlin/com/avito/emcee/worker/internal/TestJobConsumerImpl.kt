package com.avito.emcee.worker.internal

import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.DeviceConfiguration
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
            with(job.bucket.payloadContainer.payload) {
                val executor = deviceProvider.provide(
                    DeviceConfiguration(
                        testConfiguration.deviceType,
                        testConfiguration.sdkVersion
                    )
                )
                executor.beforeTestBucket()
                val applicationPackage = "stub"
                val testPackage = "stub"
                val instrumentationRunnerClass = "stub"
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
                            testMaximumDuration = Duration.seconds(
                                testConfiguration.testMaximumDuration
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
