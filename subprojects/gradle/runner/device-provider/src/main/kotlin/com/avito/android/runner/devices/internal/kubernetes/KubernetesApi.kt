package com.avito.android.runner.devices.internal.kubernetes

import com.avito.instrumentation.internal.reservation.adb.waitForCondition
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.KubernetesClient

internal interface KubernetesApi {
    val namespace: String
    suspend fun deletePod(podName: String): Boolean
    suspend fun getPodLogs(podName: String): String
    suspend fun getPod(podName: String): Pod?
    suspend fun getPodDescription(podName: String): String
    suspend fun deleteDeployment(deploymentName: String)
    suspend fun createDeployment(deployment: Deployment)
    suspend fun getPods(deploymentName: String): List<Pod>

    class Impl(
        private val kubernetesClient: KubernetesClient,
        loggerFactory: LoggerFactory,
    ) : KubernetesApi {

        private val logger = loggerFactory.create<KubernetesApi>()

        override val namespace: String = kubernetesClient.namespace

        override suspend fun deletePod(podName: String): Boolean {
            return kubernetesClient.pods().withName(podName).delete()
        }

        override suspend fun getPodLogs(podName: String): String {
            return kubernetesClient.pods().withName(podName).log
        }

        override suspend fun getPod(podName: String): Pod? {
            return kubernetesClient.pods().withName(podName).get()
        }

        override suspend fun getPodDescription(podName: String): String {
            return try {
                val actualPod = getPod(podName)
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

        override suspend fun deleteDeployment(deploymentName: String) {
            try {
                logger.debug("Deleting deployment: $deploymentName")
                kubernetesClient.apps().deployments().withName(deploymentName).delete()
                logger.debug("Deployment: $deploymentName deleted")
            } catch (t: Throwable) {
                logger.warn("Failed to delete deployment $deploymentName", t)
            }
        }

        override suspend fun createDeployment(deployment: Deployment) {
            logger.debug("Deployment.create(): start $this")
            kubernetesClient.apps().deployments().create(deployment)
            logger.debug("Deployment.create(): client returned")

            waitForDeploymentCreationDone(deployment.metadata.name, deployment.spec.replicas)
        }

        private suspend fun waitForDeploymentCreationDone(
            deploymentName: String,
            count: Int
        ) {
            logger.debug("waitForDeploymentCreationDone name=$deploymentName count=$count")
            val isDeploymentDone = waitForCondition(
                logger = logger,
                conditionName = "Deployment $deploymentName deployed"
            ) {
                getPods(
                    deploymentName = deploymentName
                ).size == count
            }
            if (!isDeploymentDone) {
                throw RuntimeException("Can't create deployment: $deploymentName")
            }
        }

        override suspend fun getPods(deploymentName: String): List<Pod> = try {

            logger.debug("Getting pods for deployment: $deploymentName")

            val pods = kubernetesClient.pods()
                .withLabel("deploymentName", deploymentName)
                .list()
                .items

            val runningPods = pods.filter { it.status.phase == POD_STATUS_RUNNING }

            val pendingPods = pods.filter { it.status.phase == POD_STATUS_PENDING }

            if (pendingPods.isNotEmpty()) {

                val containerState = pendingPods.firstOrNull()
                    ?.status
                    ?.containerStatuses
                    ?.firstOrNull()
                    ?.state

                val waitingMessage = containerState
                    ?.waiting
                    ?.message

                // waiting means pod can't start on this node
                // https://kubernetes.io/docs/tasks/debug-application-cluster/debug-application/#my-pod-stays-waiting
                if (!waitingMessage.isNullOrBlank()) {
                    logger.warn("Can't start pod: $waitingMessage")

                    // handle special cases
                    if (waitingMessage.contains("couldn't parse image reference")
                        || waitingMessage.contains("pull access denied for")
                    ) {
                        error("Can't create pods for deployment, check image reference: $waitingMessage")
                    }
                }

                val terminatedMessage = containerState
                    ?.terminated
                    ?.message

                if (!terminatedMessage.isNullOrBlank()) {
                    logger.warn("Pod terminated with message: $terminatedMessage")
                }
            }

            logger.debug(
                "Getting pods for deployment: $deploymentName completed. " +
                    "Received ${pods.size} pods (running: ${runningPods.size})."
            )

            pods
        } catch (t: Throwable) {
            logger.warn("Failed to get pods for deployment: $deploymentName", t)
            emptyList()
        }
    }

    companion object {
        const val POD_STATUS_RUNNING = "Running"
        const val POD_STATUS_PENDING = "Pending"
    }
}
