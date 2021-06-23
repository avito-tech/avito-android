package com.avito.android.runner.devices

import com.avito.android.runner.devices.internal.AndroidDebugBridgeProvider
import com.avito.android.runner.devices.internal.DeviceProviderFactoryImpl
import com.avito.android.runner.devices.internal.EmulatorsLogsReporterProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClientProvider
import com.avito.android.runner.devices.model.DeviceType
import com.avito.logger.LoggerFactory
import com.avito.runner.service.DeviceWorkerPoolProvider
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import com.avito.utils.ProcessRunner
import java.io.File

public interface DevicesProviderFactory {

    public fun create(
        tempLogcatDir: File,
        deviceWorkerPoolProvider: DeviceWorkerPoolProvider
    ): DevicesProvider

    public companion object {
        public fun create(
            loggerFactory: LoggerFactory,
            timeProvider: TimeProvider,
            deviceType: DeviceType,
            kubernetesReservationClientProvider: KubernetesReservationClientProvider,
            androidDebugBridgeProvider: AndroidDebugBridgeProvider,
            emulatorsLogsReporterProvider: EmulatorsLogsReporterProvider,
            metricsConfig: RunnerMetricsConfig,
            processRunner: ProcessRunner
        ): DevicesProviderFactory {
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
}
