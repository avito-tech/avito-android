package com.avito.runner.service

import com.avito.logger.LoggerFactory
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.worker.listener.DeviceListener
import com.avito.time.TimeProvider
import java.io.File

class DeviceWorkerPoolProvider(
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory,
    private val testListenerProvider: (File) -> TestListener,
    private val deviceListener: DeviceListener
) {

    fun provide(
        testRunnerOutputDir: File,
        state: DeviceWorkerPool.State
    ): DeviceWorkerPool {
        return DeviceWorkerPoolImpl(
            outputDirectory = testRunnerOutputDir,
            timeProvider = timeProvider,
            loggerFactory = loggerFactory,
            testListener = testListenerProvider(testRunnerOutputDir),
            deviceListener = deviceListener,
            state = state
        )
    }
}
