package com.avito.utils.gradle

import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import java.io.File

fun createKubernetesClient(
    kubernetesCredentials: KubernetesCredentials,
    namespace: String
): KubernetesClient {
    val config = when (kubernetesCredentials) {
        is KubernetesCredentials.Service -> {
            ConfigBuilder()
                .withCaCertData(kubernetesCredentials.caCertData)
                .withMasterUrl(kubernetesCredentials.url)
                .withOauthToken(kubernetesCredentials.token)
                .withNamespace(namespace)
                .build()
        }
        is KubernetesCredentials.Config -> {
            require(kubernetesCredentials.context.isNotBlank()) { "kubernetes.context should be set" }

            val configFile = File(kubernetesCredentials.configFile)
            require(configFile.exists() && configFile.length() > 0) { "kubernetes.configFile:(${kubernetesCredentials.configFile}) is unavailable" }

            val configContents = configFile.readText()
            
            Config.fromKubeconfig(kubernetesCredentials.context, configContents, "").apply {
                if (!kubernetesCredentials.caCertFile.isNullOrBlank()) {
                    caCertFile = kubernetesCredentials.caCertFile
                }

                // work with multiple namespaces/contexts not supported
                require(getNamespace() == namespace) {
                    "kubernetes.context.namespace should be $namespace. " +
                        "Namespace hardcoded in plugin, and this check only prevents from using wrong context"
                }
            }
        }
    }

    return DefaultKubernetesClient(config)
}
