package com.avito.runner.service

import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

public class DeviceWorkerPoolState(
    public val intentions: ReceiveChannel<Intention>,
    public val intentionResults: SendChannel<IntentionResult>,
    public val deviceSignals: SendChannel<Device.Signal>,
    public val devices: ReceiveChannel<Device>,
)
