package com.avito.runner.scheduler.runner

import com.avito.runner.logging.Logger
import com.avito.runner.scheduler.runner.client.TestExecutionClient
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.TestRunResult
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.IntentionExecutionService

class TestRunnerImplementation(
    private val scheduler: TestExecutionScheduler,
    private val client: TestExecutionClient,
    private val service: IntentionExecutionService,
    private val logger: Logger
) : TestRunner {

    override suspend fun runTests(tests: List<TestRunRequest>): TestRunnerResult {
        val serviceCommunication = service.start()
        val clientCommunication = client.start(
            executionServiceCommunication = serviceCommunication
        )
        val schedulerCommunication = scheduler.start(
            requests = tests,
            executionClient = clientCommunication
        )

        val expectedResultsCount = tests.count()
        val results: MutableList<TestRunResult> = mutableListOf()

        for (result in schedulerCommunication.result) {
            results += result

            logger.log(
                "Result for test: ${result.request.testCase.testName} " +
                    "received after ${result.result.size} tries. Progress (${results.count()}/$expectedResultsCount)"
            )

            if (results.count() >= expectedResultsCount) {
                break
            }
        }

        scheduler.stop()
        client.stop()
        service.stop()

        return TestRunnerResult(
            runs = results
                .map {
                    it.request to it.result
                }
                .toMap()
        )
    }
}
