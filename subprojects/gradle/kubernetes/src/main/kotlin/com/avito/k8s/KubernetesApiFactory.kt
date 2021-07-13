package com.avito.k8s

import com.avito.logger.LoggerFactory

public class KubernetesApiFactory(
    private val kubernetesClientFactory: KubernetesClientFactory,
    private val loggerFactory: LoggerFactory,
) {

    public fun create(): KubernetesApi {
        return KubernetesApiImpl(kubernetesClientFactory.create(), loggerFactory)
    }
}
