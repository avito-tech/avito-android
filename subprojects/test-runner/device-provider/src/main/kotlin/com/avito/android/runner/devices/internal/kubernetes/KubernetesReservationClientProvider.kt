package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.KubernetesApiProvider
import com.avito.android.runner.devices.internal.AndroidDebugBridgeProvider
import com.avito.android.runner.devices.internal.EmulatorsLogsReporterProvider
import com.avito.logger.LoggerFactory
import java.io.File

internal class KubernetesReservationClientProvider(
    private val loggerFactory: LoggerFactory,
    private val kubernetesApiProvider: KubernetesApiProvider,
    private val reservationDeploymentFactoryProvider: ReservationDeploymentFactoryProvider,
    private val emulatorsLogsReporterProvider: EmulatorsLogsReporterProvider,
    private val androidDebugBridgeProvider: AndroidDebugBridgeProvider
) {

    fun provide(
        tempLogcatDir: File
    ): KubernetesReservationClient {
        return KubernetesReservationClient(
            androidDebugBridge = androidDebugBridgeProvider.provide(),
            kubernetesApi = kubernetesApiProvider.provide(),
            reservationDeploymentFactory = reservationDeploymentFactoryProvider.provide(),
            loggerFactory = loggerFactory,
            emulatorsLogsReporter = emulatorsLogsReporterProvider.provide(tempLogcatDir)
        )
    }
}
