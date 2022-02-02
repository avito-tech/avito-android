package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.AndroidDebugBridgeProvider
import com.avito.android.runner.devices.internal.EmulatorsLogsReporterProvider
import com.avito.k8s.KubernetesApiFactory
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import java.io.File

public class KubernetesReservationClientProvider(
    private val loggerFactory: LoggerFactory,
    private val kubernetesApiFactory: KubernetesApiFactory,
    private val reservationDeploymentFactoryProvider: ReservationDeploymentFactoryProvider,
    private val emulatorsLogsReporterProvider: EmulatorsLogsReporterProvider,
    private val androidDebugBridgeProvider: AndroidDebugBridgeProvider,
    private val kubernetesReservationListenerProvider: KubernetesReservationListenerProvider,
    private val deviceSignals: Channel<Device.Signal>,
) {

    @ExperimentalCoroutinesApi
    internal fun provide(
        tempLogcatDir: File
    ): KubernetesReservationClient {
        val kubernetesApi = kubernetesApiFactory.create()
        val emulatorsLogsReporter = emulatorsLogsReporterProvider.provide(tempLogcatDir)
        return KubernetesReservationClientFactory(
            kubernetesApi = kubernetesApi,
            reservationDeploymentFactory = reservationDeploymentFactoryProvider.provide(),
            emulatorsLogsReporter = emulatorsLogsReporter,
            deviceProvider = RemoteDeviceProviderImpl(
                kubernetesApi,
                emulatorsLogsReporter,
                androidDebugBridgeProvider.provide(),
                loggerFactory
            ),
            listener = kubernetesReservationListenerProvider.provide(),
            loggerFactory = loggerFactory,
            podsQueryIntervalMs = 5000L,
            dispatcher = Dispatchers.IO,
            deviceSignals = deviceSignals,
        ).create()
    }
}
