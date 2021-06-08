package com.avito.runner.test.mock

import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunActionResult
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.service.worker.device.Device
import com.avito.runner.test.generateDeviceTestCaseRun
import com.avito.runner.test.generateTestCaseRun
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import java.util.Queue

class StubDeviceWorkerPool(
    testIntentionExecutionResults: List<TestCaseRun.Result> = emptyList(),
    val intentions: Channel<Intention> = Channel(Channel.UNLIMITED),
    val results: Channel<IntentionResult> = Channel(Channel.UNLIMITED),
    val deviceSignals: Channel<Device.Signal> = Channel(Channel.UNLIMITED)
) : DeviceWorkerPool {

    private val testIntentionExecutionResultsQueue: Queue<TestCaseRun.Result> =
        ArrayDeque(testIntentionExecutionResults)

    override fun start(scope: CoroutineScope) {
        scope.launch {
            for (intention in intentions) {
                if (testIntentionExecutionResultsQueue.isEmpty()) {
                    throw IllegalArgumentException(
                        "Test intention execution results is empty in MockIntentionExecutionService"
                    )
                }

                results.send(
                    IntentionResult(
                        intention = intention,
                        actionResult = InstrumentationTestRunActionResult(
                            testCaseRun = generateDeviceTestCaseRun(
                                testCaseRun = generateTestCaseRun(
                                    result = testIntentionExecutionResultsQueue.poll()
                                )
                            )
                        )
                    )
                )
            }
        }
    }

    override fun stop() {
        // no op
//        intentions.close()
//        results.close()
//        deviceSignals.close()
    }
}
