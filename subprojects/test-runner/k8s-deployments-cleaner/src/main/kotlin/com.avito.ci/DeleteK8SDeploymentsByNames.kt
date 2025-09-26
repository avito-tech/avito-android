package com.avito.ci

import io.fabric8.kubernetes.client.KubernetesClient

internal class DeleteK8SDeploymentsByNames(
    private val kubernetesClient: KubernetesClient
) {

    fun delete(
        namespace: String,
        deploymentNames: List<String>
    ) {
        try {
            val deployments = kubernetesClient
                .apps()
                .deployments()
                .inNamespace(namespace)

            deploymentNames.forEach { deployment ->
                try {
                    deployments
                        .withName(deployment)
                        .withGracePeriod(0)
                        .withTimeoutInMillis(10_000) // Performs the delete operation as blocking
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
