package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.reservation.adb.RemoteDevice
import com.avito.instrumentation.reservation.client.ReservationClient
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.instrumentation.util.waitForCondition
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.Serial
import com.avito.utils.logging.CILogger
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.KubernetesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.distinctBy
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@Suppress("EXPERIMENTAL_API_USAGE")
class KubernetesReservationClient(
    private val androidDebugBridge: AndroidDebugBridge,
    private val kubernetesClient: KubernetesClient,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    private val logger: CILogger,
    private val reservationDeploymentFactory: ReservationDeploymentFactory
) : ReservationClient {

    private var state: State = State.Idling

    override fun claim(
        reservations: Collection<Reservation.Data>,
        scope: CoroutineScope
    ): ReservationClient.ClaimResult {
        val serialsChannel = Channel<DeviceCoordinate>(Channel.UNLIMITED)
        scope.launch(Dispatchers.IO) {
            if (state !is State.Idling) {
                throw IllegalStateException("Unable to start reservation job. Already started")
            }
            val podsChannel = Channel<Pod>()
            val deploymentsChannel = Channel<String>(reservations.size)
            state = State.Reserving(pods = podsChannel, deployments = deploymentsChannel)

            reservations.forEach { reservation ->
                val deployment = reservationDeploymentFactory.createDeployment(
                    namespace = kubernetesClient.namespace,
                    reservation = reservation
                )
                val deploymentName = deployment.metadata.name
                deploymentsChannel.send(deploymentName)
                deployment.create()
                logger.debug("Deployment created: $deploymentName")

                launch {
                    listenPodsFromDeployment(
                        deploymentName = deploymentName,
                        podsChannel = podsChannel
                    )
                }
            }

            //todo use Flow
            launch {
                for (pod in podsChannel
                    .filter { it.status.phase == POD_STATUS_RUNNING }
                    .distinctBy { it.metadata.name }) {
                    launch {
                        val podName = pod.metadata.name
                        logger.debug("Found new pod: $podName")
                        val device = getDevice(pod)
                        val serial = device.serial
                        val isReady = device.waitForBoot()
                        if (isReady) {
                            emulatorsLogsReporter.redirectLogcat(
                                emulatorName = serial,
                                device = device
                            )
                            serialsChannel.send(DeviceCoordinate.Kubernetes(
                                serial = serial,
                                podName = podName
                            ))

                            logger.debug("Pod $podName sent outside for further usage")
                        } else {
                            logger.warn("Pod $podName can't load device. Disconnect and delete")
                            val isDisconnected = device.disconnect().isSuccess()
                            logger.warn("Disconnect device $serial: $isDisconnected. Can't boot it.")
                            val isDeleted = kubernetesClient.pods().withName(podName).delete()
                            logger.warn("Pod $podName is deleted: $isDeleted")
                        }
                    }
                }
            }

        }
        return ReservationClient.ClaimResult(
            deviceCoordinates = serialsChannel
        )
    }

    override suspend fun release() = withContext(Dispatchers.IO) {
        val state = state
        if (state !is State.Reserving) {
            // TODO: check on client side beforehand
            // TODO this leads to deployment leak
            throw RuntimeException("Unable to stop reservation job. Hasn't started yet")
        } else {
            state.pods.close()
            state.deployments.close()
            for (deploymentName in state.deployments.toList()) {
                launch {
                    val runningPods = podsFromDeployment(
                        deploymentName = deploymentName
                    ).filter { it.status.phase == POD_STATUS_RUNNING }

                    if (runningPods.isNotEmpty()) {
                        logger.debug("Save emulators logs for deployment: $deploymentName")
                        for (pod in runningPods) {
                            launch {
                                val podName = pod.metadata.name
                                val device = getDevice(pod)
                                val serial = device.serial
                                try {
                                    logger.debug("Saving emulator logs for pod: $podName with serial: $serial...")
                                    val podLogs = kubernetesClient.pods().withName(podName).log
                                    logger.debug("Emulators logs saved for pod: $podName with serial: $serial")

                                    logger.debug("Saving logcat for pod: $podName with serial: $serial...")
                                    emulatorsLogsReporter.reportEmulatorLogs(
                                        emulatorName = serial,
                                        log = podLogs
                                    )
                                    logger.debug("Logcat saved for pod: $podName with serial: $serial")
                                } catch (throwable: Throwable) {
                                    // TODO must be fixed after adding affinity to POD
                                    val podDescription = getPodDescription(podName)
                                    logger.warn(
                                        "Get logs from emulator failed; pod=$podName; podDescription=$podDescription; container serial=$serial",
                                        throwable
                                    )
                                }

                                logger.debug("Disconnecting device: $serial")
                                device.disconnect().fold(
                                    { logger.debug("Disconnecting device: $serial successfully completed") },
                                    { logger.warn("Failed to disconnect device: $serial") }
                                )
                            }
                        }
                    }
                }
            }
            this@KubernetesReservationClient.state = State.Idling
        }
    }

    private fun getDevice(pod: Pod): RemoteDevice {
        requireNotNull(pod.status.podIP) { "Pod: ${pod.metadata.name} must has an IP" }

        val serial = emulatorSerialName(
            name = pod.status.podIP
        )
        val device = androidDebugBridge.getRemoteDevice(
            serial = serial
        )
        return device
    }

    private fun getPodDescription(podName: String?): String {
        return try {
            val actualPod = kubernetesClient.pods().withName(podName).get()
            if (actualPod != null) {
                "[podStatus=${actualPod.status}]"
            } else {
                "pod doesn't exist"
            }
        } catch (e: Exception) {
            logger.warn("Can't get pod info", e)
            "Error when get pod description, ${e.message}"
        }
    }

    private fun removeEmulatorsDeployment(
        deploymentName: String
    ) {
        try {
            logger.debug("Deleting deployment: $deploymentName")
            kubernetesClient.apps().deployments().withName(deploymentName).delete()
            logger.debug("Deployment: $deploymentName deleted")
        } catch (t: Throwable) {
            logger.warn("Failed to delete deployment $deploymentName", t)
        }
    }

    private suspend fun Deployment.create() {
        kubernetesClient.apps().deployments().create(this)
        waitForDeploymentCreationDone(metadata.name, spec.replicas)
    }

    private suspend fun waitForDeploymentCreationDone(
        deploymentName: String,
        count: Int
    ) {
        val isDeploymentDone = waitForCondition(
            logger = { logger.debug(it) },
            conditionName = "Deployment $deploymentName deployed"
        ) {
            podsFromDeployment(
                deploymentName = deploymentName
            ).size == count
        }
        if (!isDeploymentDone) {
            throw RuntimeException("Can't create deployment: $deploymentName")
        }
    }

    private fun podsFromDeployment(
        deploymentName: String
    ): List<Pod> = try {
        logger.debug("Getting pods for deployment: $deploymentName")
        val items = kubernetesClient.pods().withLabel("deploymentName", deploymentName).list().items
        val runningPods = items.filter { it.status.phase == POD_STATUS_RUNNING }
        logger.debug(
            "Getting pods for deployment: $deploymentName completed. " +
                "Received ${items.size} pods (running: ${runningPods.size})."
        )

        items
    } catch (t: Throwable) {
        logger.warn("Failed to get pods for deployment: $deploymentName", t)
        emptyList()
    }

    private suspend fun listenPodsFromDeployment(
        deploymentName: String,
        podsChannel: SendChannel<Pod>
    ) {
        logger.debug("Start listening devices for $deploymentName")
        var pods = podsFromDeployment(deploymentName)

        while (!podsChannel.isClosedForSend && pods.isNotEmpty()) {
            pods.forEach { pod ->
                podsChannel.send(pod)
            }

            delay(TimeUnit.SECONDS.toMillis(5))

            pods = podsFromDeployment(deploymentName)
        }
        logger.debug("Finish listening devices for $deploymentName")
    }

    private fun emulatorSerialName(name: String): Serial.Remote = Serial.Remote("$name:$ADB_DEFAULT_PORT")

    private sealed class State {
        class Reserving(
            val pods: Channel<Pod>,
            val deployments: Channel<String>
        ) : State()

        object Idling : State()
    }
}

private const val ADB_DEFAULT_PORT = 5555
private const val POD_STATUS_RUNNING = "Running"
