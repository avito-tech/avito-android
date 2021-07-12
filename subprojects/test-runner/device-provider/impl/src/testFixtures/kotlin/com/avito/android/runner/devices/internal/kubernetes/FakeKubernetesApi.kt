package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.Result
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apps.Deployment

internal class FakeKubernetesApi(
    override val namespace: String = "fake-namespace"
) : KubernetesApi {

    var createDeployment: (Deployment) -> Unit = {}
    var getPods: (String) -> Result<List<KubePod>> = { Result.Success(emptyList()) }
    var deletePod: (String) -> Boolean = { true }

    override suspend fun deletePod(podName: String): Boolean {
        return deletePod.invoke(podName)
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

    override suspend fun getPods(deploymentName: String): Result<List<KubePod>> {
        return getPods.invoke(deploymentName)
    }
}
