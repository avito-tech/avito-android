package com.avito.runner.scheduler.args

import com.avito.logger.LoggerFactory
import com.avito.runner.reservation.DeviceReservation
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig

class TestRunnerFactoryConfig(
    val loggerFactory: LoggerFactory,
    val listener: TestLifecycleListener,
    val reservation: DeviceReservation,
    val metricsConfig: RunnerMetricsConfig,
    val fetchLogcatForIncompleteTests: Boolean,
    val saveTestArtifactsToOutputs: Boolean,
)
