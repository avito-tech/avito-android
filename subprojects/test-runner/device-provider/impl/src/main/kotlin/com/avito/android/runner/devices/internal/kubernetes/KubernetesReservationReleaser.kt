package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.EmulatorsLogsReporter
import com.avito.k8s.KubernetesApi
import com.avito.k8s.model.KubePod
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

internal class KubernetesReservationReleaser(
    private val kubernetesApi: KubernetesApi,
    private val deviceProvider: RemoteDeviceProvider,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    loggerFactory: LoggerFactory
) {
    private val logger = loggerFactory.create<KubernetesReservationReleaser>()

    suspend fun release(
        pods: Channel<KubePod>,
        deployments: Channel<String>
    ) {
        pods.close()
        deployments.close()
        for (deploymentName in deployments.toList()) {
            with(CoroutineScope(coroutineContext + CoroutineName("delete-deployment-$deploymentName"))) {
                launch {
                    kubernetesApi.getPods(deploymentName)
                        .onSuccess { pods -> releasePods(deploymentName, pods) }
                        .onFailure { error -> logger.warn("Can't get pods when release", error) }
                    kubernetesApi.deleteDeployment(deploymentName)
                }
            }
        }
    }

    suspend fun releaseDeployment(
        deploymentName: String
    ) {
        with(CoroutineScope(coroutineContext + CoroutineName("delete-deployment-$deploymentName"))) {
            launch {
                kubernetesApi.getPods(deploymentName)
                    .onSuccess { pods -> releasePods(deploymentName, pods) }
                    .onFailure { error -> logger.warn("Can't get pods when release", error) }
                kubernetesApi.deleteDeployment(deploymentName)
            }
        }
    }

    private suspend fun releasePods(deploymentName: String, pods: List<KubePod>) {
        val runningPods = pods.filter { it.phase is KubePod.PodPhase.Running }
        if (runningPods.isNotEmpty()) {
            logger.debug("Save emulators logs for deployment: $deploymentName")
            runningPods.forEach { pod -> releasePod(pod) }
        }
    }

    private suspend fun releasePod(pod: KubePod) {
        with(CoroutineScope(coroutineContext + CoroutineName("get-pod-logs-${pod.name}"))) {
            launch {
                val podName = pod.name
                deviceProvider.get(pod).onSuccess { device ->
                    val serial = device.serial
                    try {
                        emulatorsLogsReporter.reportEmulatorLogs(
                            pod = pod,
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
}
