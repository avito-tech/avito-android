package com.avito.emcee.internal

import com.avito.emcee.EmceeTestAction
import com.avito.emcee.EmceeTestActionConfig
import com.avito.emcee.queue.QueueApi
import com.avito.emcee.queue.ScheduleTestsBody
import com.avito.emcee.queue.ScheduledTests
import com.avito.emcee.queue.TestConfiguration
import com.avito.emcee.queue.TestEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

internal class EmceeTestActionImpl(
    private val queueApi: QueueApi,
    private val uploader: FileUploader,
    private val testsParser: TestsParser,
    private val waiter: JobWaiter
) : EmceeTestAction {

    @ExperimentalTime
    override fun execute(config: EmceeTestActionConfig) {
        runBlocking {
            withContext(Dispatchers.IO) {
                val testConfigurationFactory = createFactory(config)
                // TODO filter for specific Sdk
                val tests: List<TestEntry> = testsParser.parse(config.testApk).getOrThrow()
                try {
                    config.devices.map { device ->
                        async {
                            queueApi.scheduleTests(
                                createBody(
                                    config,
                                    testConfigurationFactory.create(device),
                                    tests
                                )
                            )
                        }
                    }.forEach {
                        it.await()
                    }
                } catch (e: Throwable) {
                    // TODO clean up successfully scheduled tests
                    throw e
                }
                waiter.wait(config.job, Duration.minutes(60))
            }
        }
    }

    private fun createBody(
        config: EmceeTestActionConfig,
        configuration: TestConfiguration,
        tests: List<TestEntry>
    ): ScheduleTestsBody {
        return ScheduleTestsBody(
            prioritizedJob = config.job,
            scheduleStrategy = config.scheduleStrategy,
            tests = ScheduledTests(
                config = ScheduledTests.Config(configuration),
                testEntries = tests
            )
        )
    }

    private suspend fun createFactory(
        config: EmceeTestActionConfig
    ): TestConfigurationFactory =
        withContext(Dispatchers.IO) {
            val apkUrl = async { uploader.upload(config.apk) }
            val testApkUrl = async { uploader.upload(config.testApk) }
            TestConfigurationFactory(
                apkUrl = apkUrl.await(),
                testApkUrl = testApkUrl.await(),
                testMaximumDurationSec = config.testMaximumDurationSec,
                testExecutionBehavior = config.testExecutionBehavior
            )
        }
}
