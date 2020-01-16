package com.avito.instrumentation.reservation.client

import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.instrumentation.util.launchGroupedCoroutines
import com.avito.runner.scheduler.args.Serial
import com.avito.utils.logging.CILogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

data class TargetGroup(val name: String, val reservation: Reservation)

fun withDevices(
    logger: CILogger,
    client: ReservationClient,
    configurationName: String,
    tests: List<TestWithTarget>,
    runType: TestExecutor.RunType,
    action: (devices: Channel<Serial>) -> Unit
) {
    val testsGroupedByTargets: Map<TargetGroup, List<TestWithTarget>> = tests.groupBy {
        TargetGroup(
            name = it.target.name,
            reservation = when (runType) {
                is TestExecutor.RunType.Run -> it.target.reservation
                is TestExecutor.RunType.Rerun -> it.target.rerunReservation
            }
        )
    }

    val reservations = testsGroupedByTargets
        .map { (target, tests) ->
            val reservation = target.reservation.data(
                tests = tests.map { it.test.name }
            )

            logger.info(
                "Devices: ${reservation.count} devices will be allocated for " +
                    "target: ${target.name} inside configuration: $configurationName"
            )

            reservation
        }

    try {
        val serialsChannel = Channel<String>(Channel.UNLIMITED)

        launchGroupedCoroutines {
            launch(blocking = false) {
                logger.info("Devices: Starting reservation job for configuration: $configurationName...")
                client.claim(
                    reservations = reservations,
                    serialsChannel = serialsChannel
                )
                logger.info("Devices: Reservation job completed for configuration: $configurationName")
            }
            launch {
                logger.info("Devices: Starting action for configuration: $configurationName...")
                action(serialsChannel)
                logger.info("Devices: Action completed for configuration: $configurationName")
            }
        }

    } catch (e: Exception) {
        logger.critical("Error during starting reservation job", e)
    } finally {
        logger.info("Devices: Starting releasing devices for configuration: $configurationName...")
        runBlocking {
            client.release(reservations = reservations)
        }
        logger.info("Devices: Devices released for configuration: $configurationName")
    }
}
