package com.avito.emcee.worker.internal

import com.avito.emcee.queue.Device
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal interface TestExecutorProvider {

    suspend fun provide(device: Device): TestExecutor
}
