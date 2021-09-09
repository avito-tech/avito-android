package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.EmulatorsLogsReporter
import com.avito.k8s.KubernetesApi
import com.avito.logger.LoggerFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex

@ExperimentalCoroutinesApi
internal class KubernetesReservationClientFactory(
    private val kubernetesApi: KubernetesApi,
    private val reservationDeploymentFactory: ReservationDeploymentFactory,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    private val deviceProvider: RemoteDeviceProvider,
    private val loggerFactory: LoggerFactory,
    private val podsQueryIntervalMs: Long,
    private val dispatcher: CoroutineDispatcher
) {

    fun create(): KubernetesReservationClient {
        val lock = Mutex()
        return KubernetesReservationClient(
            claimer(lock),
            releaser(),
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
            lock,
            loggerFactory
        )

    private fun releaser(): KubernetesReservationReleaser =
        KubernetesReservationReleaser(
            kubernetesApi, deviceProvider, emulatorsLogsReporter, loggerFactory
        )
}
