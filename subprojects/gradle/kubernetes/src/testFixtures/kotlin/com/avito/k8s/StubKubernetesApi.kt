package com.avito.k8s

import com.avito.android.Result
import com.avito.k8s.model.KubePod
import io.fabric8.kubernetes.api.model.apps.Deployment

public class StubKubernetesApi(
    override val namespace: String = "stub-namespace"
) : KubernetesApi {

    public var createDeployment: (Deployment) -> Unit = {}

    public var getPods: (String) -> Result<List<KubePod>> = { Result.Success(emptyList()) }

    public var deletePod: (String) -> Boolean = { true }

    override suspend fun deletePod(podName: String): Boolean {
        return deletePod.invoke(podName)
    }

    override suspend fun getPodLogs(podName: String): String {
        return "stub-pod-logs"
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
