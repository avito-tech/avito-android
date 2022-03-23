package com.avito.emcee.worker.internal

import com.avito.emcee.queue.DeviceConfiguration

internal interface TestExecutorProvider {

    suspend fun provide(device: DeviceConfiguration): TestExecutor
}
