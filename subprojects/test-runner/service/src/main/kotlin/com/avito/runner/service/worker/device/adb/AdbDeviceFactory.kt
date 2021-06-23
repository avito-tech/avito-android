package com.avito.runner.service.worker.device.adb

import com.avito.android.Result
import com.avito.android.stats.StatsDSender
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.adb.listener.AdbDeviceEventsListener
import com.avito.runner.service.worker.device.adb.listener.AdbDeviceEventsLogger
import com.avito.runner.service.worker.device.adb.listener.AdbDeviceMetrics
import com.avito.runner.service.worker.device.adb.listener.CompositeAdbDeviceEventListener
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import com.avito.utils.ProcessRunner

class AdbDeviceFactory(
    private val loggerFactory: LoggerFactory,
    private val adb: Adb,
    private val timeProvider: TimeProvider,
    private val metricsConfig: RunnerMetricsConfig?,
    private val processRunner: ProcessRunner
) {

    fun create(
        coordinate: DeviceCoordinate,
        adbDeviceParams: AdbDeviceParams
    ): Result<AdbDevice> {
        val logger = loggerFactory.create("[${coordinate.serial}]")
        val listener = createEventListener(logger)
        return getSdkVersion(listener).get(coordinate.serial)
            .map { sdk ->
                AdbDevice(
                    coordinate = coordinate,
                    model = adbDeviceParams.model,
                    online = adbDeviceParams.online,
                    api = sdk,
                    adb = adb,
                    timeProvider = timeProvider,
                    logger = logger,
                    eventsListener = listener
                )
            }
    }

    private fun getSdkVersion(listener: AdbDeviceEventsListener): GetSdkVersion {
        return GetSdkVersion(
            processRunner = processRunner,
            retryAction = RetryAction(timeProvider),
            adb = adb,
            eventsListener = listener
        )
    }

    private fun createEventListener(
        logger: Logger,
    ): AdbDeviceEventsListener {
        return if (metricsConfig == null) {
            AdbDeviceEventsLogger(logger)
        } else {
            CompositeAdbDeviceEventListener(
                listOf(
                    AdbDeviceEventsLogger(logger),
                    AdbDeviceMetrics(
                        statsDSender = StatsDSender.Impl(
                            config = metricsConfig.statsDConfig,
                            loggerFactory = loggerFactory
                        ),
                        runnerPrefix = metricsConfig.runnerPrefix
                    )
                )
            )
        }
    }
}
