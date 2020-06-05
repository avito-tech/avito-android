package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.reservation.adb.RemoteDevice
import com.avito.instrumentation.reservation.client.ReservationClient
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.instrumentation.util.forEachAsync
import com.avito.instrumentation.util.iterateInParallel
import com.avito.instrumentation.util.waitForCondition
import com.avito.runner.service.worker.device.Serial
import com.avito.utils.logging.CILogger
import com.fkorotkov.kubernetes.apps.metadata
import com.fkorotkov.kubernetes.apps.newDeployment
import com.fkorotkov.kubernetes.apps.selector
import com.fkorotkov.kubernetes.apps.spec
import com.fkorotkov.kubernetes.apps.template
import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newContainer
import com.fkorotkov.kubernetes.newEnvVar
import com.fkorotkov.kubernetes.newHostPathVolumeSource
import com.fkorotkov.kubernetes.newToleration
import com.fkorotkov.kubernetes.newVolume
import com.fkorotkov.kubernetes.newVolumeMount
import com.fkorotkov.kubernetes.resources
import com.fkorotkov.kubernetes.securityContext
import com.fkorotkov.kubernetes.spec
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodSpec
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.KubernetesClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.distinctBy
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

@Suppress("EXPERIMENTAL_API_USAGE")
class KubernetesReservationClient(
    private val androidDebugBridge: AndroidDebugBridge,
    private val kubernetesClient: KubernetesClient,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    private val configurationName: String,
    private val projectName: String,
    private val buildId: String,
    private val buildType: String,
    private val logger: CILogger,
    private val registry: String
) : ReservationClient {

    private var state: State = State.Idling

    init {
        require(buildId.isNotBlank()) { "buildId is blank, client can't distinguish reservations" }
    }

    override suspend fun claim(
        reservations: Collection<Reservation.Data>,
        serialsChannel: SendChannel<Serial>,
        reservationDeployments: SendChannel<String> // TODO: make this state internal
    ) {
        if (state !is State.Idling) {
            val error = RuntimeException("Unable to start reservation job. Already started")
            logger.critical(error.message.orEmpty())
            throw error
        }
        logger.debug("Starting deployments for configuration: $configurationName...")
        val podsChannel = Channel<Pod>()
        state = State.Reserving(pods = podsChannel)

        reservations.forEach { reservation ->
            val deploymentName = generateDeploymentName()
            reservationDeployments.send(deploymentName)

            logger.debug("Starting deployment: $deploymentName")
            when (reservation.device) {
                is Device.Emulator -> getEmulatorDeployment(
                    emulator = reservation.device,
                    deploymentName = deploymentName,
                    count = reservation.count
                )
                is Device.Phone -> getDeviceDeployment(
                    count = reservation.count,
                    phone = reservation.device,
                    deploymentName = deploymentName
                )
                is Device.LocalEmulator -> throw IllegalStateException(
                    "Local emulator ${reservation.device} is unsupported in kubernetes reservation"
                )
                is Device.CloudEmulator -> getCloudEmulatorDeployment(
                    emulator = reservation.device,
                    deploymentName = deploymentName,
                    count = reservation.count
                )
            }.create()
            logger.debug("Deployment created: $deploymentName")

            listenPodsFromDeployment(
                deploymentName = deploymentName,
                podsChannel = podsChannel
            )
        }

        //todo use Flow
        @Suppress("DEPRECATION")
        podsChannel
            .filter { it.status.phase == POD_STATUS_RUNNING }
            .distinctBy { it.metadata.name }
            .forEachAsync { pod ->
                logger.info("Found new pod: ${pod.metadata.name}")
                requireNotNull(pod.status.podIP) { "pod has ip after deployment" }

                val serial = emulatorSerialName(
                    name = pod.status.podIP
                )
                val device = androidDebugBridge.getDevice(
                    serial = serial
                )
                check(device is RemoteDevice)
                val isReady = device.waitForBoot()
                if (isReady) {
                    emulatorsLogsReporter.redirectLogcat(
                        emulatorName = serial,
                        device = device
                    )
                    serialsChannel.send(serial)

                    logger.info("Pod ${pod.metadata.name} sent outside for further usage")
                } else {
                    logger.info("Pod ${pod.metadata.name} can't load device. Disconnect and delete")
                    val isDisconnected = device.disconnect().isSuccess()
                    logger.info("Disconnect device $serial: $isDisconnected. Can't boot it.")
                    val isDeleted = kubernetesClient.pods().withName(pod.metadata.name).delete()
                    logger.info("Pod ${pod.metadata.name} is deleted: $isDeleted")
                }
            }
    }

    override suspend fun release(
        reservationDeployments: Collection<String>
    ) {
        if (state !is State.Reserving) {
            // TODO: check on client side beforehand
            val error = RuntimeException("Unable to stop reservation job. Hasn't started yet")
            logger.critical(error.message.orEmpty())
            throw error
        }
        (state as State.Reserving).pods.close()

        logger.info("Releasing devices for configuration: $configurationName...")

        reservationDeployments
            .iterateInParallel { _, deploymentName ->

                val runningPods = podsFromDeployment(
                    deploymentName = deploymentName
                ).filter { it.status.phase == POD_STATUS_RUNNING }

                if (runningPods.isNotEmpty()) {
                    logger.info("Save emulators logs for deployment: $deploymentName")
                    runningPods
                        .iterateInParallel { _, pod ->
                            val podName = pod.metadata.name
                            requireNotNull(pod.status.podIP) { "pod has ip before removal" }

                            val serial = emulatorSerialName(
                                name = pod.status.podIP
                            )
                            val device = androidDebugBridge.getDevice(
                                serial = serial
                            )
                            check(device is RemoteDevice)

                            logger.info("Saving emulator logs for pod: $podName with serial: $serial...")
                            try {
                                val podLogs = kubernetesClient.pods().withName(podName).log
                                logger.info("Emulators logs saved for pod: $podName with serial: $serial")

                                logger.info("Saving logcat for pod: $podName with serial: $serial...")
                                emulatorsLogsReporter.reportEmulatorLogs(
                                    emulatorName = serial,
                                    log = podLogs
                                )
                                logger.info("Logcat saved for pod: $podName with serial: $serial")
                            } catch (throwable: Throwable) {
                                // TODO must be fixed after adding affinity to POD
                                val podDescription = getPodDescription(podName)
                                logger.critical(
                                    "Get logs from emulator failed; pod=$podName; podDescription=$podDescription; container serial=$serial",
                                    RuntimeException(podDescription, throwable)
                                )
                            }

                            logger.info("Disconnecting device: $serial")
                            device.disconnect().fold(
                                { logger.info("Disconnecting device: $serial successfully completed") },
                                { logger.info("Failed to disconnect device: $serial") }
                            )
                        }
                    logger.info("Emulators logs saved for deployment: $deploymentName")
                }

                logger.info("Deleting deployment: $deploymentName...")
                removeEmulatorsDeployment(
                    deploymentName = deploymentName
                )
                logger.info("Deployment: $deploymentName deleted")
            }

        state = State.Idling

        logger.info("Devices released for configuration: $configurationName")
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
            logger.info("Can't get pod info", e)
            "Error when get pod description, ${e.message}"
        }
    }

    private fun removeEmulatorsDeployment(
        deploymentName: String
    ) {
        try {
            kubernetesClient.apps().deployments().withName(deploymentName).delete()
        } catch (t: Throwable) {
            logger.critical("Delete deployment $deploymentName", t)
        }
    }

    private fun getDeviceDeployment(
        count: Int,
        phone: Device.Phone,
        deploymentName: String,
        kubernetesNodeName: String = "avi-training06" //temporary node, remove later
    ): Deployment {
        return deviceDeployment(
            deploymentMatchLabels = deviceMatchLabels(phone),
            deploymentName = deploymentName,
            count = count
        ) {
            containers = listOf(
                newContainer {
                    name = phone.name.kubernetesName()
                    image = "$registry/${phone.proxyImage}"

                    securityContext {
                        privileged = true
                    }
                    resources {
                        limits = mapOf(
                            "android/device" to Quantity("1")
                        )
                        requests = mapOf(
                            "android/device" to Quantity("1")
                        )
                    }
                }
            )
            dnsPolicy = "ClusterFirst"
            nodeName = kubernetesNodeName
            tolerations = listOf(
                newToleration {
                    operator = "Exists"
                    effect = "NoSchedule"
                }
            )
        }
    }

    @Deprecated("remove after 2020.6.1")
    private fun getEmulatorDeployment(
        emulator: Device.Emulator,
        deploymentName: String,
        count: Int
    ): Deployment {
        return deviceDeployment(
            deploymentMatchLabels = deviceMatchLabels(emulator),
            deploymentName = deploymentName,
            count = count
        ) {
            containers = listOf(
                newContainer {
                    name = emulator.name.kubernetesName()
                    image = "$registry/${emulator.image}"

                    securityContext {
                        privileged = true
                    }

                    resources {
                        limits = mapOf(
                            "cpu" to Quantity(emulator.cpuCoresLimit),
                            "memory" to Quantity("3.5Gi")
                        )
                        requests = mapOf(
                            "cpu" to Quantity(emulator.cpuCoresRequest)
                        )
                    }

                    if (emulator.gpu) {
                        volumeMounts = listOf(
                            newVolumeMount {
                                name = "x-11"
                                mountPath = "/tmp/.X11-unix"
                                readOnly = true
                            }
                        )

                        env = listOf(
                            newEnvVar {
                                name = "GPU_ENABLED"
                                value = "true"
                            },
                            newEnvVar {
                                name = "SNAPSHOT_DISABLED"
                                value = "true"
                            }
                        )
                    }
                }
            )

            if (emulator.gpu) {
                volumes = listOf(
                    newVolume {
                        name = "x-11"

                        hostPath = newHostPathVolumeSource {
                            path = "/tmp/.X11-unix"
                            type = "Directory"
                        }
                    }
                )
            }

            tolerations = listOf(
                newToleration {
                    key = "dedicated"
                    operator = "Equal"
                    value = "android"
                    effect = "NoSchedule"
                }
            )
        }
    }

    private fun getCloudEmulatorDeployment(
        emulator: Device.CloudEmulator,
        deploymentName: String,
        count: Int
    ): Deployment {
        return deviceDeployment(
            deploymentMatchLabels = deviceMatchLabels(emulator),
            deploymentName = deploymentName,
            count = count
        ) {
            containers = listOf(
                newContainer {
                    name = emulator.name.kubernetesName()
                    image = "$registry/${emulator.image}"

                    securityContext {
                        privileged = true
                    }

                    resources {
                        limits = mapOf(
                            "cpu" to Quantity(emulator.cpuCoresLimit),
                            "memory" to Quantity(emulator.memoryLimit)
                        )
                        requests = mapOf(
                            "cpu" to Quantity(emulator.cpuCoresRequest)
                        )
                    }

                    if (emulator.gpu) {
                        volumeMounts = listOf(
                            newVolumeMount {
                                name = "x-11"
                                mountPath = "/tmp/.X11-unix"
                                readOnly = true
                            }
                        )

                        env = listOf(
                            newEnvVar {
                                name = "GPU_ENABLED"
                                value = "true"
                            },
                            newEnvVar {
                                name = "SNAPSHOT_DISABLED"
                                value = "true"
                            }
                        )
                    }
                }
            )

            if (emulator.gpu) {
                volumes = listOf(
                    newVolume {
                        name = "x-11"

                        hostPath = newHostPathVolumeSource {
                            path = "/tmp/.X11-unix"
                            type = "Directory"
                        }
                    }
                )
            }

            tolerations = listOf(
                newToleration {
                    key = "dedicated"
                    operator = "Equal"
                    value = "android"
                    effect = "NoSchedule"
                }
            )
        }
    }

    private fun deviceDeployment(
        deploymentMatchLabels: Map<String, String>,
        deploymentName: String,
        count: Int,
        block: PodSpec.() -> Unit
    ): Deployment {
        val deploymentSpecificationsMatchLabels = deploymentMatchLabels
            .plus("deploymentName" to deploymentName)

        return newDeployment {
            apiVersion = "extensions/v1beta1"
            metadata {
                name = deploymentName
                labels = deploymentMatchLabels
                finalizers = listOf(
                    // Remove all dependencies (replicas) in foreground after removing deployment
                    "foregroundDeletion"
                )
            }
            spec {
                replicas = count
                selector {
                    matchLabels = deploymentSpecificationsMatchLabels
                }

                template {
                    metadata {
                        labels = deploymentSpecificationsMatchLabels
                    }
                    spec(block)
                }
            }
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
            logger = { logger.info(it) },
            conditionName = "Deployment $deploymentName deployed"
        ) {
            podsFromDeployment(
                deploymentName = deploymentName
            ).size == count
        }
        if (!isDeploymentDone) {
            val error =
                RuntimeException("Something went wrong during deploying deployment: $deploymentName")
            logger.critical(error.message.orEmpty())
            throw error
        }
    }

    private fun podsFromDeployment(
        deploymentName: String
    ): List<Pod> = try {
        logger.info("Getting pods for deployment: $deploymentName")
        val items = kubernetesClient.pods().withLabel("deploymentName", deploymentName).list().items
        val runningPods = items.filter { it.status.phase == POD_STATUS_RUNNING }
        logger.info(
            "Getting pods for deployment: $deploymentName completed. " +
                "Received ${items.size} pods (running: ${runningPods.size})."
        )

        items
    } catch (t: Throwable) {
        logger.info("Failed to get pods for deployment: $deploymentName", t)
        emptyList()
    }

    private fun listenPodsFromDeployment(
        deploymentName: String,
        podsChannel: SendChannel<Pod>
    ) {
        // TODO: Don't use global scope. Unconfined coroutines lead to leaks
        GlobalScope.launch {
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
    }

    private fun generateDeploymentName(): String =
        "${kubernetesClient.namespace}-${UUID.randomUUID()}"
            .kubernetesName()

    private fun deviceMatchLabels(
        device: Device
    ): Map<String, String> {
        return mapOf(
            "type" to buildType, // teamcity or local
            "id" to buildId, // teamcity_build_id or local synthetic
            "project" to projectName,
            "instrumentationConfiguration" to configurationName,
            "device" to device.description
        )
    }

    private fun emulatorSerialName(name: String): Serial = Serial.Remote("$name:$ADB_DEFAULT_PORT")

    private fun String.kubernetesName(): String = replace("_", "-").toLowerCase()

    sealed class State {
        class Reserving(
            val pods: Channel<Pod>
        ) : State()

        object Idling : State()
    }
}

private const val ADB_DEFAULT_PORT = 5555
private const val POD_STATUS_RUNNING = "Running"
