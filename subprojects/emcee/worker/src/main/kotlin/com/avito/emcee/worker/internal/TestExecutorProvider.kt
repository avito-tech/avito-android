package com.avito.emcee.worker.internal

import com.avito.emcee.queue.Device

internal interface TestExecutorProvider {

    suspend fun provide(device: Device): TestExecutor
}
