package com.avito.runner.test.mock

import com.avito.runner.service.IntentionExecutionService
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunActionResult
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.IntentionResult
import com.avito.runner.test.generateDeviceTestCaseRun
import com.avito.runner.test.generateTestCaseRun
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import java.util.Queue

class MockIntentionExecutionService(
    testIntentionExecutionResults: List<TestCaseRun.Result> = emptyList()
) : IntentionExecutionService {

    private val testIntentionExecutionResultsQueue: Queue<TestCaseRun.Result> =
        ArrayDeque(testIntentionExecutionResults)

    private val intentions: Channel<Intention> = Channel(Channel.UNLIMITED)
    private val results: Channel<IntentionResult> = Channel(Channel.UNLIMITED)

    override fun start(): IntentionExecutionService.Communication {
        // TODO: Don't use global scope. Unconfined coroutines lead to leaks
        GlobalScope.launch {
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

        return IntentionExecutionService.Communication(
            intentions = intentions,
            results = results
        )
    }

    override fun stop() {}
}
