package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.EmulatorsLogsReporter
import com.avito.android.runner.devices.internal.RemoteDevice
import com.avito.android.runner.devices.model.ReservationData
import com.avito.k8s.KubernetesApi
import com.avito.k8s.model.KubePod
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.distinctBy
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

@ExperimentalCoroutinesApi
internal class KubernetesReservationClaimer(
    private val reservationDeploymentFactory: ReservationDeploymentFactory,
    private val kubernetesApi: KubernetesApi,
    private val deploymentPodsListener: DeploymentPodsListener,
    private val deviceProvider: RemoteDeviceProvider,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    private val kubernetesReservationListener: KubernetesReservationListener,
    private val lock: Mutex,
    loggerFactory: LoggerFactory,
    private val deviceSignals: Channel<Device.Signal>,
) {

    private val logger = loggerFactory.create<KubernetesReservationClaimer>()

    suspend fun claim(
        reservations: Collection<ReservationData>,
        serialsChannel: Channel<DeviceCoordinate>,
        podsChannel: Channel<KubePod>,
        deploymentsChannel: Channel<String>
    ) = with(CoroutineScope(coroutineContext)) {
        reservations.forEach { reservation ->
            launch(CoroutineName("create-deployment")) {
                createDeployment(
                    reservation,
                    deploymentsChannel,
                    podsChannel,
                    serialsChannel
                )
            }
        }
        initializeDevices(podsChannel, serialsChannel)
    }

    private suspend fun createDeployment(
        reservation: ReservationData,
        deploymentsChannel: Channel<String>,
        podsChannel: Channel<KubePod>,
        serialsChannel: Channel<DeviceCoordinate>
    ) {
        val deployment = reservationDeploymentFactory.createDeployment(
            namespace = kubernetesApi.namespace,
            reservation = reservation
        )
        val deploymentName = lock.withLock {
            val deploymentName = deployment.metadata.name
            deploymentsChannel.send(deploymentName)
            deviceSignals.send(Device.Signal.NewDeployment(deploymentName, reservation.device.name))
            kubernetesApi.createDeployment(deployment)
            deploymentName
        }

        deploymentPodsListener.start(
            deploymentName, podsChannel
        ).onFailure {
            podsChannel.cancel()
            serialsChannel.close()
        }
    }

    private suspend fun initializeDevices(
        podsChannel: ReceiveChannel<KubePod>,
        serialsChannel: Channel<DeviceCoordinate>
    ) {
        with(CoroutineScope(coroutineContext + CoroutineName("waiting-pods"))) {
            launch(CoroutineName("waiting-pods")) {
                @Suppress("DEPRECATION")
                podsChannel
                    .map {
                        if (serialsChannel.isClosedForSend) {
                            logger.debug("cancel waiting-pods")
                            podsChannel.cancel()
                        }
                        it
                    }
                    .filter { it.phase is KubePod.PodPhase.Running }
                    .distinctBy { it.name }
                    .consumeEach { pod ->
                        kubernetesReservationListener.onPodAcquired()
                        launch(CoroutineName("boot-pod-${pod.name}")) {
                            deviceProvider.create(pod)
                                .onSuccess { device ->
                                    device.sendTo(
                                        pod.name,
                                        serialsChannel
                                    )
                                }
                        }
                    }
                logger.debug("initializeDevices finished")
            }
        }
    }

    private suspend fun RemoteDevice.sendTo(
        podName: String,
        serials: Channel<DeviceCoordinate>,
    ) {
        if (serials.isClosedForSend) {
            logger.debug("Pod $podName boot device but serials channel closed")
            return
        }
        emulatorsLogsReporter.redirectLogcat(
            emulatorName = serial,
            device = this
        )
        serials.send(
            DeviceCoordinate.Kubernetes(
                serial = serial,
                podName = podName
            )
        )

        logger.debug("Pod $podName sent outside for further usage")
    }
}
