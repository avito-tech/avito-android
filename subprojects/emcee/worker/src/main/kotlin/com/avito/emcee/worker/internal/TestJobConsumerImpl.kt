package com.avito.emcee.worker.internal

import com.avito.emcee.queue.BuildArtifacts
import com.avito.emcee.queue.DeviceConfiguration
import com.avito.emcee.worker.internal.artifacts.FileDownloader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class TestJobConsumerImpl(
    private val deviceProvider: TestExecutorProvider,
    private val fileDownloader: FileDownloader,
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
                            testMaximumDuration = Duration.seconds(
                                testConfiguration.testMaximumDurationSec
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
        val apk = fileDownloader.download(buildArtifacts.app.location.url.toHttpUrl())
        val testApk = fileDownloader.download(buildArtifacts.testApp.location.url.toHttpUrl())
        return apk to testApk
    }
}
