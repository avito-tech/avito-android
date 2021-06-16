package com.avito.android.runner.devices

import com.avito.android.runner.devices.internal.AndroidDebugBridgeProvider
import com.avito.android.runner.devices.internal.DeviceProviderFactoryImpl
import com.avito.android.runner.devices.internal.EmulatorsLogsReporterProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClientProvider
import com.avito.android.runner.devices.internal.kubernetes.ReservationDeploymentFactoryProvider
import com.avito.android.runner.devices.model.DeviceType
import com.avito.android.stats.StatsDConfig
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import com.avito.utils.gradle.KubernetesCredentials
import java.io.File

public interface DevicesProviderFactory {
    public fun create(tempLogcatDir: File): DevicesProvider

    public companion object {
        public fun create(
            loggerFactory: LoggerFactory,
            timeProvider: TimeProvider,
            deviceType: DeviceType,
            kubernetesNamespace: String,
            credentials: KubernetesCredentials,
            statsDConfig: StatsDConfig,
            configurationName: String,
            projectName: String,
            buildId: String,
            buildType: String,
            logcatTags: Collection<String>,
            outputDir: File,
            metricsConfig: RunnerMetricsConfig
        ): DevicesProviderFactory {
            val androidDebugBridgeProvider = AndroidDebugBridgeProvider(loggerFactory)
            val emulatorsLogsReporterProvider = EmulatorsLogsReporterProvider(
                logcatTags = logcatTags,
                outputDir = outputDir
            )
            return DeviceProviderFactoryImpl(
                loggerFactory = loggerFactory,
                timeProvider = timeProvider,
                deviceType = deviceType,
                kubernetesReservationClientProvider = KubernetesReservationClientProvider(
                    loggerFactory = loggerFactory,
                    kubernetesApiProvider = KubernetesApiProvider(
                        timeProvider = timeProvider,
                        kubernetesNamespace = kubernetesNamespace,
                        kubernetesCredentials = credentials,
                        loggerFactory = loggerFactory,
                        statsDConfig = statsDConfig
                    ),
                    reservationDeploymentFactoryProvider = ReservationDeploymentFactoryProvider(
                        configurationName = configurationName,
                        projectName = projectName,
                        buildId = buildId,
                        buildType = buildType,
                        loggerFactory = loggerFactory
                    ),
                    emulatorsLogsReporterProvider = emulatorsLogsReporterProvider,
                    androidDebugBridgeProvider = androidDebugBridgeProvider
                ),
                androidDebugBridgeProvider = androidDebugBridgeProvider,
                emulatorsLogsReporterProvider = emulatorsLogsReporterProvider,
                metricsConfig = metricsConfig
            )
        }
    }
}
