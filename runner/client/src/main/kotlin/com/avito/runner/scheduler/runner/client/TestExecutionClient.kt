package com.avito.runner.scheduler.runner.client

import com.avito.runner.scheduler.runner.client.model.ClientTestRunRequest
import com.avito.runner.scheduler.runner.client.model.ClientTestRunResult
import com.avito.runner.scheduler.runner.scheduler.TestExecutionState
import com.avito.runner.service.IntentionExecutionService
import com.avito.runner.service.model.intention.ActionResult
import com.avito.runner.service.model.intention.Intention
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class TestExecutionClient {

    private val statesMapping: MutableMap<Intention, TestExecutionState> = mutableMapOf()

    private val requests: Channel<ClientTestRunRequest> = Channel(Channel.UNLIMITED)
    private val results: Channel<ClientTestRunResult> = Channel(Channel.UNLIMITED)

    fun start(executionServiceCommunication: IntentionExecutionService.Communication): Communication {
        GlobalScope.launch {
            for (request in requests) {
                statesMapping[request.intention] = request.state
                executionServiceCommunication.intentions.send(request.intention)
            }
        }

        GlobalScope.launch {
            for (serviceResult in executionServiceCommunication.results) {
                when (serviceResult.actionResult) {
                    is ActionResult.InstrumentationTestRunActionResult -> {
                        val sourceState: TestExecutionState = statesMapping[serviceResult.intention]
                            ?: throw RuntimeException("State for intention ${serviceResult.intention} not found in mapping")

                        results.send(
                            ClientTestRunResult(
                                state = sourceState,
                                incomingTestCaseRun = (serviceResult.actionResult as ActionResult.InstrumentationTestRunActionResult)
                                    .testCaseRun
                            )
                        )
                    }
                }
            }
        }

        return Communication(
            requests = requests,
            results = results
        )
    }

    fun stop() {
        requests.close()
        results.close()
    }

    class Communication(
        val requests: SendChannel<ClientTestRunRequest>,
        val results: ReceiveChannel<ClientTestRunResult>
    )
}
