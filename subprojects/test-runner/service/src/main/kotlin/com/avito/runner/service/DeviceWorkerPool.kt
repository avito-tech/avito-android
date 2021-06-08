package com.avito.runner.service

import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

interface DeviceWorkerPool {

    fun start(scope: CoroutineScope)

    fun stop()

    class State(
        val intentions: ReceiveChannel<Intention>,
        val intentionResults: SendChannel<IntentionResult>,
        val deviceSignals: SendChannel<Device.Signal>,
        val devices: ReceiveChannel<Device>,
    )
}
