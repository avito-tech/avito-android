package com.avito.runner.scheduler.runner.client

import com.avito.logger.NoOpLogger
import com.avito.runner.scheduler.StubTestExecutionState
import com.avito.runner.scheduler.runner.client.model.ClientTestRunRequest
import com.avito.runner.scheduler.util.generateTestRunRequest
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.State
import com.avito.runner.test.generateInstrumentationTestAction
import com.avito.runner.test.generateIntention
import com.avito.runner.test.mock.StubIntentionExecutionService
import com.avito.runner.test.receiveAvailable
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class TestExecutionClientTest {

    private val logger = NoOpLogger

    @Test
    fun `test execution client return results for every request`() =
        runBlockingTest {
            val requests = listOf(
                ClientTestRunRequest(
                    state = StubTestExecutionState(
                        request = generateTestRunRequest()
                    ),
                    intention = generateIntention(
                        state = State(
                            layers = listOf(
                                State.Layer.ApiLevel(api = 22)
                            )
                        ),
                        action = generateInstrumentationTestAction()
                    )
                ),
                ClientTestRunRequest(
                    state = StubTestExecutionState(
                        request = generateTestRunRequest()
                    ),
                    intention = generateIntention(
                        state = State(
                            layers = listOf(
                                State.Layer.ApiLevel(api = 22)
                            )
                        ),
                        action = generateInstrumentationTestAction()
                    )
                )
            )

            val service = StubIntentionExecutionService(
                testIntentionExecutionResults = requests.map {
                    TestCaseRun.Result.Passed
                }
            )
            val serviceCommunication = service.start(this)

            val client = TestExecutionClient(TestCoroutineDispatcher(), logger)
            val clientCommunication = client.start(serviceCommunication, this)

            requests.forEach { clientCommunication.requests.send(it) }

            val results = clientCommunication.results.receiveAvailable()
            service.stop()
            client.stop()

            assertWithMessage("Results received for every request")
                .that(
                    results
                        .map {
                            (it.state as StubTestExecutionState).request
                        }
                )
                .isEqualTo(
                    requests.map { it.state.request }
                )
            assertWithMessage("All received results are passed")
                .that(
                    results
                        .map {
                            it.incomingTestCaseRun.testCaseRun.result
                        }
                )
                .isEqualTo(
                    requests.map { TestCaseRun.Result.Passed }
                )
        }
}
