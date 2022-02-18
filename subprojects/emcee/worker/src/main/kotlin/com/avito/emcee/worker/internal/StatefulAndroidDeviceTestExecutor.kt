package com.avito.emcee.worker.internal

import com.avito.emcee.device.AndroidApplication
import com.avito.emcee.device.AndroidDevice

internal class StatefulAndroidDeviceTestExecutor(
    private val device: AndroidDevice
) : TestExecutor {

    private var state: AndroidDeviceState = AndroidDeviceState.Clean(device)

    override suspend fun beforeTestBucket() {
        // empty
    }

    override suspend fun afterTestBucket() {
        /**
         * TODO
         * current logic correct but ineffective if two buckets use same apps.
         * Optimization could be to uninstall apps only if in a new bucket we got new apps
         */
        state = state.clean()
    }

    override suspend fun execute(
        job: TestExecutor.Job
    ): TestExecutor.Result {
        device.isAlive()
        state = state.prepareStateForExecution(
            listOf(
                AndroidApplication(job.apk, job.applicationPackage),
                AndroidApplication(job.testApk, job.testPackage)
            )
        )
        val result = executeTest(job)
        state = state.testExecuted()
        return result
    }

    private suspend fun executeTest(job: TestExecutor.Job): TestExecutor.Result {
        // todo add timeout
        device.executeInstrumentation(
            // todo pass envs
            listOf(
                "${job.testPackage}/${job.instrumentationRunnerClass}"
            )
        )
        TODO("Not yet implemented")
    }
}
