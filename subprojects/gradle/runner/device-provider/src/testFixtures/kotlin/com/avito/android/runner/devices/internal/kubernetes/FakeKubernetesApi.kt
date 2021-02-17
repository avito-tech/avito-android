package com.avito.android.runner.devices.internal.kubernetes

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.Deployment

internal class FakeKubernetesApi(
    override val namespace: String = "fake-namespace"
) : KubernetesApi {

    var createDeployment: (Deployment) -> Unit = {}
    var getPods: (String) -> List<Pod> = { emptyList() }

    override suspend fun deletePod(podName: String): Boolean {
        return true
    }

    override suspend fun getPodLogs(podName: String): String {
        return "stub-pod-logs"
    }

    override suspend fun getPod(podName: String): Pod? {
        return null
    }

    override suspend fun getPodDescription(podName: String): String {
        return "stub-pod-description"
    }

    override suspend fun deleteDeployment(deploymentName: String) {
        // empty
    }

    override suspend fun createDeployment(deployment: Deployment) {
        createDeployment.invoke(deployment)
    }

    override suspend fun getPods(deploymentName: String): List<Pod> {
        return getPods.invoke(deploymentName)
    }
}
