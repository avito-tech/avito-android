package com.avito.runner.scheduler.runner.client

import com.avito.runner.scheduler.runner.client.model.ClientTestRunRequest
import com.avito.runner.scheduler.util.generateTestRunRequest
import com.avito.runner.scheduler.util.mock.MockTestExecutionState
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.State
import com.avito.runner.test.generateInstrumentationTestAction
import com.avito.runner.test.generateIntention
import com.avito.runner.test.mock.MockIntentionExecutionService
import com.avito.runner.test.receiveAvailable
import com.avito.runner.test.runBlockingWithTimeout
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class TestExecutionClientTest {

    @Test
    fun `test execution client return results for every request`() =
        runBlockingWithTimeout {
            val requests = listOf(
                ClientTestRunRequest(
                    state = MockTestExecutionState(
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
                    state = MockTestExecutionState(
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

            val service = MockIntentionExecutionService(
                testIntentionExecutionResults = requests.map {
                    TestCaseRun.Result.Passed
                }
            )
            val serviceCommunication = service.start()

            val client = TestExecutionClient()
            val clientCommunication = client.start(serviceCommunication)

            requests.forEach { clientCommunication.requests.send(it) }

            delay(TimeUnit.SECONDS.toMillis(3))

            val results = clientCommunication.results.receiveAvailable()

            assertWithMessage("Results received for every request")
                .that(
                    results
                        .map {
                            (it.state as MockTestExecutionState).request
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
