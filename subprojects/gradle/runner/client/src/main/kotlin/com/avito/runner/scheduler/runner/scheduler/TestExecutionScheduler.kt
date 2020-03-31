package com.avito.runner.scheduler.runner.scheduler

import com.avito.runner.logging.Logger
import com.avito.runner.scheduler.runner.client.TestExecutionClient
import com.avito.runner.scheduler.runner.client.model.ClientTestRunRequest
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.TestRunResult
import com.avito.runner.scheduler.runner.scheduler.retry.SchedulingBasedRetryManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

class TestExecutionScheduler(
    private val logger: Logger
) {

    private val resultChannel: Channel<TestRunResult> = Channel(Channel.UNLIMITED)

    fun start(
        requests: List<TestRunRequest>,
        executionClient: TestExecutionClient.Communication
    ): Communication {

        // TODO: Don't use global scope. Unconfined coroutines lead to leaks
        GlobalScope.launch {
            for (testRunResult in executionClient.results) {

                when (val verdict = testRunResult.state.verdict(testRunResult.incomingTestCaseRun)) {
                    is TestExecutionState.Verdict.SendResult -> {
                        resultChannel.send(
                            TestRunResult(
                                request = testRunResult.state.request,
                                result = verdict.results
                            )
                        )
                    }
                    is TestExecutionState.Verdict.Run -> {
                        verdict.intentions.forEach { intention ->
                            logger.log("Retry intention: $intention")
                            executionClient.requests.send(
                                ClientTestRunRequest(
                                    state = testRunResult.state,
                                    intention = intention
                                )
                            )
                        }
                    }
                }
            }
        }

        // TODO: Don't use global scope. Unconfined coroutines lead to leaks
        GlobalScope.launch {
            for (request in requests) {
                val testState =
                    TestExecutionStateImplementation(
                        request = request,
                        retryManager = SchedulingBasedRetryManager(
                            scheduling = request.scheduling
                        )
                    )

                when (val verdict = testState.verdict()) {
                    is TestExecutionState.Verdict.Run -> {
                        verdict.intentions.forEach { intention ->
                            executionClient.requests.send(
                                ClientTestRunRequest(
                                    state = testState,
                                    intention = intention
                                )
                            )
                        }
                    }
                    is TestExecutionState.Verdict.SendResult -> {
                        throw RuntimeException("Trying to send empty result")
                    }
                }
            }
        }

        return Communication(
            result = resultChannel
        )
    }

    fun stop() {
        resultChannel.close()
    }

    class Communication(
        val result: ReceiveChannel<TestRunResult>
    )
}
