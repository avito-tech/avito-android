package com.avito.runner.service

import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

interface IntentionExecutionService {

    fun start(scope: CoroutineScope): Communication

    fun stop()

    class Communication(
        val intentions: SendChannel<Intention>,
        val results: ReceiveChannel<IntentionResult>,
        val deviceSignals: ReceiveChannel<Device.Signal>
    )
}
