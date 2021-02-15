package com.avito.runner.scheduler.runner

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.runner.client.TestExecutionClient
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.TestRunResult
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.IntentionExecutionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

class TestRunnerImplementation(
    private val scheduler: TestExecutionScheduler,
    private val client: TestExecutionClient,
    private val service: IntentionExecutionService,
    private val reservationWatcher: DeviceReservationWatcher,
    loggerFactory: LoggerFactory
) : TestRunner {

    private val logger = loggerFactory.create<TestRunner>()

    override suspend fun runTests(tests: List<TestRunRequest>, scope: CoroutineScope): TestRunnerResult {
        return withContext(scope.coroutineContext) {
            val serviceCommunication = service.start(this)
            reservationWatcher.watch(serviceCommunication.deviceSignals, this)
            val clientCommunication = client.start(
                executionServiceCommunication = serviceCommunication,
                scope = this
            )
            val schedulerCommunication = scheduler.start(
                requests = tests,
                executionClient = clientCommunication,
                scope = this
            )

            val expectedResultsCount = tests.count()
            val results: MutableList<TestRunResult> = mutableListOf()

            for (result in schedulerCommunication.result) {
                results += result

                logger.debug(
                    "Result for test: %s received after %d tries. Progress (%s)".format(
                        result.request.testCase.testName,
                        result.result.size,
                        "${results.count()}/$expectedResultsCount"
                    )
                )

                if (results.count() >= expectedResultsCount) {
                    break
                }
            }

            scheduler.stop()
            client.stop()
            service.stop()

            TestRunnerResult(
                runs = results
                    .map {
                        it.request to it.result
                    }
                    .toMap()
            )
        }
    }
}
