package com.avito.emcee.worker.internal

import com.avito.emcee.queue.DeviceConfiguration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal interface TestExecutorProvider {

    suspend fun provide(configuration: DeviceConfiguration): TestExecutor
}
