package com.avito.utils.gradle

import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient

fun createKubernetesClient(
    kubernetesCredentials: KubernetesCredentials,
    namespace: String
): KubernetesClient {
    return DefaultKubernetesClient(
        ConfigBuilder()
            .withCaCertData(kubernetesCredentials.caCertData)
            .withMasterUrl(kubernetesCredentials.url)
            .withOauthToken(kubernetesCredentials.token)
            .build()
    )
        .inNamespace(namespace)
}
