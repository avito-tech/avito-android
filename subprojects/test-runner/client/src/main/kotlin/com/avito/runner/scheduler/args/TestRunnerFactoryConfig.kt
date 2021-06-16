package com.avito.runner.scheduler.args

import com.avito.logger.LoggerFactory
import com.avito.runner.reservation.DeviceReservation

internal class TestRunnerFactoryConfig(
    val loggerFactory: LoggerFactory,
    val reservation: DeviceReservation,
)
