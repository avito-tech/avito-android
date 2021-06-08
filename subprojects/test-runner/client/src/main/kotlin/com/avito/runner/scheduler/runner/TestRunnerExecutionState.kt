package com.avito.runner.scheduler.runner

import com.avito.runner.scheduler.runner.model.TestRunResult
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.channels.Channel

class TestRunnerExecutionState(
    val results: Channel<TestRunResult> = Channel(Channel.UNLIMITED),
    val intentions: Channel<Intention> = Channel(Channel.UNLIMITED),
    val intentionResults: Channel<IntentionResult> = Channel(Channel.UNLIMITED),
    val deviceSignals: Channel<Device.Signal> = Channel(Channel.UNLIMITED)
) {
    fun cancel() {
        results.cancel()
        intentions.cancel()
        intentionResults.cancel()
        deviceSignals.cancel()
    }
}
