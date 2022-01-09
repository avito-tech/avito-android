package com.avito.k8s

import com.avito.android.Result
import com.avito.k8s.model.KubePod
import io.fabric8.kubernetes.api.model.apps.Deployment

public interface KubernetesApi {

    public val namespace: String

    public val needForward: Boolean

    public suspend fun deletePod(podName: String): Boolean

    public suspend fun getPodLogs(podName: String): String

    public suspend fun getPodDescription(podName: String): String

    public suspend fun deleteDeployment(deploymentName: String)

    public suspend fun createDeployment(deployment: Deployment)

    public suspend fun getPods(deploymentName: String): Result<List<KubePod>>
}
