package com.avito.runner.scheduler.runner.scheduler

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.scheduler.runner.client.TestExecutionClient
import com.avito.runner.scheduler.runner.client.model.ClientTestRunRequest
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.TestRunResult
import com.avito.runner.scheduler.runner.scheduler.retry.SchedulingBasedRetryManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

class TestExecutionScheduler(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    loggerFactory: LoggerFactory
) {

    private val logger = loggerFactory.create<TestExecutionScheduler>()

    private val resultChannel: Channel<TestRunResult> = Channel(Channel.UNLIMITED)

    fun start(
        requests: List<TestRunRequest>,
        executionClient: TestExecutionClient.Communication,
        scope: CoroutineScope
    ): Communication {
        scope.launch(dispatcher + CoroutineName("test-state-verdict")) {
            for (testRunResult in executionClient.results) {
                when (val verdict = testRunResult.state.verdict(testRunResult.incomingTestCaseRun)) {
                    is TestExecutionState.Verdict.SendResult ->
                        resultChannel.send(
                            TestRunResult(
                                request = testRunResult.state.request,
                                result = verdict.results
                            )
                        )

                    is TestExecutionState.Verdict.Run ->
                        verdict.intentions.forEach { intention ->
                            logger.debug("Retry intention: $intention")
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
        scope.launch(dispatcher + CoroutineName("test-state-verdict")) {
            for (request in requests) {
                val testState =
                    TestExecutionStateImplementation(
                        request = request,
                        retryManager = SchedulingBasedRetryManager(
                            scheduling = request.scheduling
                        )
                    )

                when (val verdict = testState.verdict()) {
                    is TestExecutionState.Verdict.Run ->
                        verdict.intentions.forEach { intention ->
                            executionClient.requests.send(
                                ClientTestRunRequest(
                                    state = testState,
                                    intention = intention
                                )
                            )
                        }

                    is TestExecutionState.Verdict.SendResult ->
                        throw RuntimeException("Trying to send empty result")
                }
            }
        }

        return Communication(
            result = resultChannel
        )
    }

    fun stop() {
        resultChannel.cancel()
    }

    class Communication(
        val result: ReceiveChannel<TestRunResult>
    )
}
