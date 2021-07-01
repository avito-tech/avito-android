package com.avito.android.runner.devices

import com.avito.android.runner.devices.internal.AndroidDebugBridgeProvider
import com.avito.android.runner.devices.internal.DeviceProviderFactoryImpl
import com.avito.android.runner.devices.internal.EmulatorsLogsReporterProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClientProvider
import com.avito.android.runner.devices.model.DeviceType
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import com.avito.utils.ProcessRunner

public class DeviceProviderFactoryProvider(
    private val loggerFactory: LoggerFactory,
    private val timeProvider: TimeProvider,
    private val deviceType: DeviceType,
    private val kubernetesReservationClientProvider: KubernetesReservationClientProvider,
    private val androidDebugBridgeProvider: AndroidDebugBridgeProvider,
    private val emulatorsLogsReporterProvider: EmulatorsLogsReporterProvider,
    private val metricsConfig: RunnerMetricsConfig,
    private val processRunner: ProcessRunner
) {
    public fun provide(): DevicesProviderFactory {
        return DeviceProviderFactoryImpl(
            loggerFactory = loggerFactory,
            timeProvider = timeProvider,
            deviceType = deviceType,
            kubernetesReservationClientProvider = kubernetesReservationClientProvider,
            emulatorsLogsReporterProvider = emulatorsLogsReporterProvider,
            androidDebugBridgeProvider = androidDebugBridgeProvider,
            metricsConfig = metricsConfig,
            processRunner = processRunner
        )
    }
}
