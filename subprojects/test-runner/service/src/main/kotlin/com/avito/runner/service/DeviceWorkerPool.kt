package com.avito.runner.service

import kotlinx.coroutines.CoroutineScope

interface DeviceWorkerPool {

    fun start(scope: CoroutineScope)

    fun stop()
}
