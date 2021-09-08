package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.EmulatorsLogsReporter
import com.avito.android.runner.devices.internal.RemoteDevice
import com.avito.android.runner.devices.internal.ReservationClient
import com.avito.android.runner.devices.model.ReservationData
import com.avito.k8s.KubernetesApi
import com.avito.k8s.model.KubePod
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.DeviceCoordinate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * You should know that canceling jobs manually or via throwing exception
 * will cancel whole parent job and all consuming channels.
 *
 * In [KubernetesReservationClient] case podsChannel will close automatically when [claim] job failed or canceled
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class KubernetesReservationClient(
    private val deviceProvider: RemoteDeviceProvider,
    private val kubernetesApi: KubernetesApi,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    private val reservationDeploymentFactory: ReservationDeploymentFactory,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    loggerFactory: LoggerFactory,
    podsQueryIntervalMs: Long = 5000L
) : ReservationClient {

    private val logger = loggerFactory.create<KubernetesReservationClient>()
    private var state: State = State.Idling
    private val lock = Mutex()
    private val deploymentPodsListener = DeploymentPodsListener(lock, kubernetesApi, podsQueryIntervalMs, loggerFactory)
    private val reservationReleaser = KubernetesReservationReleaser(
        kubernetesApi, deviceProvider, emulatorsLogsReporter, loggerFactory
    )

    override suspend fun claim(
        reservations: Collection<ReservationData>
    ): ReservationClient.ClaimResult {
        require(reservations.isNotEmpty()) {
            "Must have at least one reservation but empty"
        }
        return lock.withLock {
            if (state !is State.Idling) {
                throw IllegalStateException("Unable claim reservation. State is already started")
            }
            // TODO close serialsChannel
            val serialsChannel = Channel<DeviceCoordinate>(Channel.UNLIMITED)
            with(CoroutineScope(coroutineContext + dispatcher)) {
                launch(CoroutineName("main-reservation")) {
                    val podsChannel = Channel<KubePod>(Channel.UNLIMITED)
                    val deploymentsChannel = Channel<String>(reservations.size)
                    state = State.Reserving(pods = podsChannel, deployments = deploymentsChannel)

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
            }

            ReservationClient.ClaimResult(
                deviceCoordinates = serialsChannel
            )
        }
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

    private fun CoroutineScope.initializeDevices(
        podsChannel: ReceiveChannel<KubePod>,
        serialsChannel: Channel<DeviceCoordinate>
    ) {
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

    override suspend fun remove(podName: String) {
        withContext(CoroutineName("delete-pod-$podName") + dispatcher) {
            kubernetesApi.deletePod(podName)
        }
    }

    override suspend fun release() = withContext(dispatcher) {
        lock.withLock {
            val state = state
            if (state !is State.Reserving) {
                // TODO: check on client side beforehand
                // TODO this leads to deployment leak
                throw IllegalStateException("Unable to stop reservation job. Hasn't started yet")
            } else {
                reservationReleaser.release(state.pods, state.deployments)
                this@KubernetesReservationClient.state = State.Idling
            }
        }
        logger.debug("release finished")
    }

    private sealed class State {
        class Reserving(
            val pods: Channel<KubePod>,
            val deployments: Channel<String>
        ) : State()

        object Idling : State()
    }

    companion object
}
