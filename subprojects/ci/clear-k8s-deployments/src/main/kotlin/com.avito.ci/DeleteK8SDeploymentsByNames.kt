package com.avito.ci

import io.fabric8.kubernetes.client.DefaultKubernetesClient

class DeleteK8SDeploymentsByNames(
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
                        .delete()
                } catch (e: Throwable) {
                    println("Error when delete deployment. namespace=$namespace; deployment=$deployment")
                    e.printStackTrace()
                }
            }
        } catch (e: Throwable) {
            println("Error when delete deployments. namespace=$namespace; deployments=$deploymentNames")
            e.printStackTrace()
        }
    }
}
