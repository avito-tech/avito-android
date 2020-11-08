package com.avito.runner.scheduler.runner.client

import com.avito.logger.Logger
import com.avito.runner.scheduler.runner.client.model.ClientTestRunRequest
import com.avito.runner.scheduler.runner.client.model.ClientTestRunResult
import com.avito.runner.scheduler.runner.scheduler.TestExecutionState
import com.avito.runner.service.IntentionExecutionService
import com.avito.runner.service.model.intention.Intention
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class TestExecutionClient(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val logger: Logger
) {

    private val statesMapping: MutableMap<Intention, TestExecutionState> = mutableMapOf()

    private val requests: Channel<ClientTestRunRequest> = Channel(Channel.UNLIMITED)
    private val results: Channel<ClientTestRunResult> = Channel(Channel.UNLIMITED)

    suspend fun start(
        executionServiceCommunication: IntentionExecutionService.Communication,
        scope: CoroutineScope
    ): Communication {
        scope.launch(dispatcher) {
            scope.launch {
                for (request in requests) {
                    statesMapping[request.intention] = request.state
                    log("sending intention: ${request.intention}")
                    executionServiceCommunication.intentions.send(request.intention)
                }
            }
            scope.launch {
                for (serviceResult in executionServiceCommunication.results) {
                    val sourceState: TestExecutionState = statesMapping[serviceResult.intention]
                        ?: throw RuntimeException("State for intention ${serviceResult.intention} not found in mapping")

                    results.send(
                        ClientTestRunResult(
                            state = sourceState,
                            incomingTestCaseRun = serviceResult.actionResult
                                .testCaseRun
                        )
                    )
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

    private fun log(message: String) {
        logger.debug("TestExecutionClient: $message")
    }

    class Communication(
        val requests: SendChannel<ClientTestRunRequest>,
        val results: ReceiveChannel<ClientTestRunResult>
    )
}
