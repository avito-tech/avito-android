package com.avito.k8s

import com.avito.logger.LoggerFactory

public class KubernetesApiFactory(
    private val kubernetesClientFactory: KubernetesClientFactory,
    private val loggerFactory: LoggerFactory,
    private val needForward: Boolean,
) {

    public fun create(): KubernetesApi {
        return KubernetesApiImpl(kubernetesClientFactory.create(), loggerFactory, needForward)
    }
}
