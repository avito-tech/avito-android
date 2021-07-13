package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.Result
import com.avito.android.runner.devices.internal.AndroidDebugBridge
import com.avito.android.runner.devices.internal.EmulatorsLogsReporter
import com.avito.android.runner.devices.internal.RemoteDevice
import com.avito.android.runner.devices.internal.ReservationClient
import com.avito.android.runner.devices.model.ReservationData
import com.avito.k8s.KubernetesApi
import com.avito.k8s.model.KubePod
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.Serial
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
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
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
    private val androidDebugBridge: AndroidDebugBridge,
    private val kubernetesApi: KubernetesApi,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    loggerFactory: LoggerFactory,
    private val reservationDeploymentFactory: ReservationDeploymentFactory,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val podsQueryIntervalMs: Long = 5000L
) : ReservationClient {

    private val logger = loggerFactory.create<KubernetesReservationClient>()

    private var state: State = State.Idling

    private val lock = Mutex()

    override suspend fun claim(
        reservations: Collection<ReservationData>
    ): ReservationClient.ClaimResult {
        require(reservations.isNotEmpty()) {
            "Must have at least one reservation but empty"
        }
        return lock.withLock {
            // TODO close serialsChannel
            val serialsChannel = Channel<DeviceCoordinate>(Channel.UNLIMITED)
            if (state !is State.Idling) {
                throw IllegalStateException("Unable claim reservation. State is already started")
            }
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

        listenPodsFromDeployment(
            deploymentName = deploymentName,
            podsChannel = podsChannel,
            serialsChannel = serialsChannel
        )
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
                        pod.initializeDevice()
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

    private suspend fun KubePod.initializeDevice(): Result<RemoteDevice> {
        val podName = name
        logger.debug("Found new pod: $podName")
        return bootDevice().onFailure { error ->
            val message = buildString {
                append("Pod $podName can't load device. Disconnect and delete.")
                val podIp = ip
                if (!podIp.isNullOrBlank()) {
                    appendLine()
                    append("Check device logs in artifacts: ${emulatorsLogsReporter.getLogFile(podIp)}")
                }
            }
            logger.warn(message, error)
            val isDeleted = kubernetesApi.deletePod(podName)
            logger.debug("Pod $podName is deleted: $isDeleted")
        }
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
                state.pods.close()
                state.deployments.close()
                for (deploymentName in state.deployments.toList()) {
                    launch(CoroutineName("delete-deployment-$deploymentName")) {
                        kubernetesApi.getPods(deploymentName).fold(
                            { pods ->
                                val runningPods = pods.filter { it.phase is KubePod.PodPhase.Running }
                                if (runningPods.isNotEmpty()) {
                                    logger.debug("Save emulators logs for deployment: $deploymentName")
                                    for (pod in runningPods) {
                                        launch(CoroutineName("get-pod-logs-${pod.name}")) {
                                            val podName = pod.name
                                            pod.getDevice().onSuccess { device ->
                                                val serial = device.serial
                                                try {
                                                    emulatorsLogsReporter.reportEmulatorLogs(
                                                        emulatorName = serial,
                                                        log = kubernetesApi.getPodLogs(podName)
                                                    )
                                                } catch (throwable: Throwable) {
                                                    // TODO must be fixed after adding affinity to POD
                                                    val podDescription = kubernetesApi.getPodDescription(podName)
                                                    logger.warn(
                                                        "Get logs from emulator failed; pod=$podName; " +
                                                            "podDescription=$podDescription; " +
                                                            "container serial=$serial",
                                                        throwable
                                                    )
                                                }
                                                device.disconnect().fold(
                                                    { logger.debug("Device: $serial disconnected") },
                                                    { error ->
                                                        logger.warn("Failed to disconnect device: $serial", error)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            { error ->
                                logger.warn("Can't get pods when release", error)
                            }
                        )
                        kubernetesApi.deleteDeployment(deploymentName)
                    }
                }
                this@KubernetesReservationClient.state = State.Idling
            }
        }
        logger.debug("release finished")
    }

    private suspend fun KubePod.bootDevice(): Result<RemoteDevice> {
        return getDevice()
            .flatMap { device ->
                device.waitForBoot()
                    .map { device }
                    .onFailure { device.disconnect() }
            }
    }

    private fun KubePod.getDevice(): Result<RemoteDevice> {
        return Result.tryCatch {
            val podIp = ip
            requireNotNull(podIp) { "Pod: $name must have an IP" }

            val serial = emulatorSerialName(name = podIp)

            androidDebugBridge.getRemoteDevice(
                serial = serial
            )
        }
    }

    private suspend fun listenPodsFromDeployment(
        deploymentName: String,
        podsChannel: Channel<KubePod>,
        serialsChannel: Channel<DeviceCoordinate>
    ) {
        logger.debug("Start listening devices for $deploymentName")

        var next = true
        while (next) {
            lock.withLock {
                if (!podsChannel.isClosedForSend) {
                    when (val result = kubernetesApi.getPods(deploymentName)) {
                        is Result.Success -> {
                            result.value.forEach { pod ->
                                podsChannel.send(pod)
                            }
                            delay(podsQueryIntervalMs)
                        }
                        is Result.Failure -> {
                            next = false
                            logger.critical("Error get pods", result.throwable)
                            podsChannel.cancel()
                            serialsChannel.close()
                        }
                    }
                } else {
                    next = false
                }
            }
        }
        logger.debug("listenPodsFromDeployment finished, [deploymentName=$deploymentName]")
    }

    private fun emulatorSerialName(name: String): Serial.Remote = Serial.Remote("$name:$ADB_DEFAULT_PORT")

    private sealed class State {
        class Reserving(
            val pods: Channel<KubePod>,
            val deployments: Channel<String>
        ) : State()

        object Idling : State()
    }

    companion object
}

private const val ADB_DEFAULT_PORT = 5555
