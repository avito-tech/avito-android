package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.EmulatorsLogsReporter
import com.avito.k8s.KubernetesApi
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex

@ExperimentalCoroutinesApi
internal class KubernetesReservationClientFactory(
    private val kubernetesApi: KubernetesApi,
    private val reservationDeploymentFactory: ReservationDeploymentFactory,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    private val deviceProvider: RemoteDeviceProvider,
    private val listener: KubernetesReservationListener,
    private val loggerFactory: LoggerFactory,
    private val podsQueryIntervalMs: Long,
    private val dispatcher: CoroutineDispatcher,
    private val deviceSignals: Channel<Device.Signal>
) {

    fun create(): KubernetesReservationClient {
        val lock = Mutex()
        return KubernetesReservationClient(
            claimer(lock),
            releaser(),
            listener,
            kubernetesApi,
            dispatcher,
            lock,
            loggerFactory
        )
    }

    private fun claimer(lock: Mutex): KubernetesReservationClaimer =
        KubernetesReservationClaimer(
            reservationDeploymentFactory,
            kubernetesApi,
            DeploymentPodsListener(lock, kubernetesApi, podsQueryIntervalMs, loggerFactory),
            deviceProvider,
            emulatorsLogsReporter,
            listener,
            lock,
            loggerFactory,
            deviceSignals = deviceSignals,
        )

    private fun releaser(): KubernetesReservationReleaser =
        KubernetesReservationReleaser(
            kubernetesApi, deviceProvider, emulatorsLogsReporter, loggerFactory
        )
}
