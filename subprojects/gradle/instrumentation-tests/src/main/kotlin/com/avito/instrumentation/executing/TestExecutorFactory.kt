package com.avito.instrumentation.executing

import com.avito.instrumentation.report.listener.TestReporter
import com.avito.instrumentation.reservation.client.ReservationClientFactory
import com.avito.utils.logging.CILogger

/**
 * Abstraction for testing purposes only
 */
interface TestExecutorFactory {

    fun createExecutor(
        logger: CILogger,
        reservationClientFactory: ReservationClientFactory,
        testReporter: TestReporter?
    ): TestExecutor

    class Implementation : TestExecutorFactory {

        override fun createExecutor(
            logger: CILogger,
            reservationClientFactory: ReservationClientFactory,
            testReporter: TestReporter?
        ): TestExecutor {
            return TestExecutor.Impl(
                logger = logger,
                reservationClientFactory = reservationClientFactory,
                testReporter = testReporter
            )
        }
    }
}
