package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.Result
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
    suspend fun getPods(deploymentName: String): Result<List<KubePod>>

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
            waitForCondition(
                logger = logger,
                conditionName = "Deployment $deploymentName deployed"
            ) {
                getPods(
                    deploymentName = deploymentName
                ).flatMap { pods ->
                    if (pods.size == count) {
                        Result.Success(pods)
                    } else {
                        Result.Failure(IllegalStateException("Pods count is ${pods.size} but expected $count"))
                    }
                }
            }.onFailure {
                throw IllegalStateException("Can't create deployment: $deploymentName", it)
            }
        }

        private fun failFastOnBadImage(pods: List<KubePod>) {
            pods.forEach { pod ->
                val phase = pod.container.phase
                if (phase is KubeContainer.ContainerPhase.Waiting) {
                    if (phase.hasProblemsGettingImage()) {
                        error("Can't create pods for deployment, check image reference: ${phase.message}")
                    }
                }
            }
        }

        override suspend fun getPods(deploymentName: String): Result<List<KubePod>> = Result.tryCatch {

            val message = StringBuilder()

            message.appendLine("Getting pods for deployment: $deploymentName:")
            message.appendLine("----------------")

            val pods: List<KubePod> = kubernetesClient.pods()
                .withLabel("deploymentName", deploymentName)
                .list()
                .items
                .map { KubePod(it) }

            pods.forEach { pod -> message.appendLine(pod.toString()) }

            failFastOnBadImage(pods)

            val runningCount = pods.count { it.phase == KubePod.PodPhase.Running }
            val pendingCount = pods.count { it.phase is KubePod.PodPhase.Pending }
            val otherCount = pods.size - runningCount - pendingCount

            message.append(
                "------- Summary: " +
                    "running: $runningCount; " +
                    "pending: $pendingCount; " +
                    "other $otherCount  ---------"
            )

            logger.debug(message.toString())

            pods
        }
    }
}
