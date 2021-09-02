package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.AndroidDebugBridgeProvider
import com.avito.android.runner.devices.internal.EmulatorsLogsReporterProvider
import com.avito.k8s.KubernetesApiFactory
import com.avito.logger.LoggerFactory
import java.io.File

public class KubernetesReservationClientProvider(
    private val loggerFactory: LoggerFactory,
    private val kubernetesApiFactory: KubernetesApiFactory,
    private val reservationDeploymentFactoryProvider: ReservationDeploymentFactoryProvider,
    private val emulatorsLogsReporterProvider: EmulatorsLogsReporterProvider,
    private val androidDebugBridgeProvider: AndroidDebugBridgeProvider
) {

    internal fun provide(
        tempLogcatDir: File
    ): KubernetesReservationClient {
        return KubernetesReservationClient(
            androidDebugBridge = androidDebugBridgeProvider.provide(),
            kubernetesApi = kubernetesApiFactory.create(),
            reservationDeploymentFactory = reservationDeploymentFactoryProvider.provide(),
            loggerFactory = loggerFactory,
            emulatorsLogsReporter = emulatorsLogsReporterProvider.provide(tempLogcatDir)
        )
    }
}
