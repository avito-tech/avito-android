package com.avito.android.runner.devices

import com.avito.android.runner.devices.internal.kubernetes.KubernetesApi
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.time.TimeProvider
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient

public class KubernetesApiProvider(
    private val timeProvider: TimeProvider,
    private val kubernetesCredentials: KubernetesCredentials,
    private val kubernetesNamespace: String,
    private val loggerFactory: LoggerFactory,
    private val statsDConfig: StatsDConfig
) {

    internal fun provide(): KubernetesApi {
        return KubernetesApi.Impl(
            kubernetesClient = createKubernetesClient(
                httpClientProvider = HttpClientProvider(
                    statsDSender = StatsDSender.Impl(statsDConfig, loggerFactory),
                    timeProvider = timeProvider,
                    loggerFactory = loggerFactory,
                ),
                kubernetesCredentials = kubernetesCredentials,
                namespace = kubernetesNamespace
            ),
            loggerFactory = loggerFactory
        )
    }
}
