package com.avito.ci

import io.fabric8.kubernetes.client.DefaultKubernetesClient

internal class DeleteK8SDeploymentsByNames(
    private val kubernetesClient: DefaultKubernetesClient
) {

    fun delete(
        namespace: String,
        deploymentNames: List<String>
    ) {
        try {
            val deployments = kubernetesClient.inNamespace(namespace)
                .apps()
                .deployments()

            deploymentNames.forEach { deployment ->
                try {
                    deployments
                        .withName(deployment)
                        .withGracePeriod(0)
                        .delete()
                } catch (e: Throwable) {
                    throw RuntimeException("Error when delete deployment=$deployment", e)
                }
            }
        } catch (e: Throwable) {
            throw RuntimeException(
                "Error when delete deployments. namespace=$namespace; deployments=$deploymentNames",
                e
            )
        }
    }
}
