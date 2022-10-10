package com.avito.emcee.worker.internal

import com.avito.emcee.queue.DeviceConfiguration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal interface DeviceTestExecutorProvider {

    suspend fun provide(configuration: DeviceConfiguration): TestExecutor
}
