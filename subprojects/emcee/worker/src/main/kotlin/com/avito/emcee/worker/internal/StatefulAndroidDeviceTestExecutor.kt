package com.avito.emcee.worker.internal

import com.avito.android.Result
import com.avito.android.device.AndroidApplication
import com.avito.android.device.AndroidDevice
import com.avito.android.device.InstrumentationCommand
import kotlinx.coroutines.withTimeout
import java.util.logging.Logger
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class StatefulAndroidDeviceTestExecutor(
    private val device: AndroidDevice
) : TestExecutor {

    private val logger = Logger.getLogger("StatefulAndroidDeviceTestExecutor")

    private var state: AndroidDeviceState = AndroidDeviceState.Clean(device)

    override suspend fun beforeTestBucket() {
        logger.fine("StatefulAndroidDeviceTestExecutor lifecycle: beforeTestBucket")
        // empty
    }

    override suspend fun afterTestBucket() {
        /**
         * TODO
         * current logic correct but ineffective if two buckets use same apps.
         * Optimization could be to uninstall apps only if in a new bucket we got new apps
         */
        logger.fine("StatefulAndroidDeviceTestExecutor lifecycle: afterTestBucket")
        state = state.clean()
    }

    override suspend fun execute(
        job: TestExecutor.Job
    ): TestExecutor.Result {
        logger.fine("Executing job: $job")
        device.isAlive()
        logger.fine("Preparing state for job: $job")
        state = state.prepareStateForExecution(
            listOf(
                AndroidApplication(job.apk, job.applicationPackage),
                AndroidApplication(job.testApk, job.testPackage)
            )
        )
        logger.fine("Executing test job: $job")
        val result = executeTest(job)
        state = state.testExecuted()
        return result
    }

    private suspend fun executeTest(job: TestExecutor.Job): TestExecutor.Result {
        return Result.tryCatch {
            val instrumentationResult = withTimeout(job.testMaximumDuration) {
                device.executeInstrumentation(
                    command = InstrumentationCommand(
                        testName = job.test.name.asInstrumentationArg(),
                        testPackage = job.testPackage,
                        runnerClass = job.instrumentationRunnerClass
                    )
                )
            }
            instrumentationResult
                .map { TestExecutor.Result(job.test, it.success) }
                .getOrThrow()
        }.getOrElse { TestExecutor.Result(job.test, false) }
    }
}
