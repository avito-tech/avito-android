package com.avito.k8s

import com.avito.android.stats.StatsDSender
import com.avito.http.RetryInterceptor
import com.avito.http.StatsHttpEventListener
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.time.TimeProvider
import com.avito.utils.gradle.KubernetesCredentials
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.OAuthTokenProvider
import io.fabric8.kubernetes.client.utils.HttpClientUtils
import io.kubernetes.client.util.FilePersister
import io.kubernetes.client.util.KubeConfig
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

public class KubernetesClientFactory(
    private val kubernetesCredentials: KubernetesCredentials,
    private val loggerFactory: LoggerFactory,
    private val timeProvider: TimeProvider,
    private val statsDSender: StatsDSender,
    private val httpTries: Int,
) {

    private val logger = loggerFactory.create<KubernetesClientFactory>()

    public fun create(): KubernetesClient {
        val config = when (kubernetesCredentials) {
            is KubernetesCredentials.Service -> ConfigBuilder()
                .withCaCertData(kubernetesCredentials.caCertData)
                .withMasterUrl(kubernetesCredentials.url)
                .withOauthToken(kubernetesCredentials.token)
                .withNamespace(kubernetesCredentials.namespace)
                .build()
            is KubernetesCredentials.Config -> {
                // todo move validation to configuration phase
                require(kubernetesCredentials.context.isNotBlank()) { "kubernetes.context should be set" }

                val configFile = kubernetesCredentials.configFile
                require(configFile.exists() && configFile.length() > 0) {
                    "kubernetes.configFile:(${kubernetesCredentials.configFile}) is unavailable"
                }

                val configContents = configFile.readText()

                Config.fromKubeconfig(kubernetesCredentials.context, configContents, "").apply {
                    val caCert = kubernetesCredentials.caCertFile
                    if (caCert != null && caCert.exists()) {
                        caCertFile = caCert.absolutePath
                    }

                    val namespaceFromKubeConfig = getNamespace()

                    val namespaceDiffErrorMessage = {
                        "kubernetes.context.namespace should be ${kubernetesCredentials.namespace}, " +
                            "but was $namespaceFromKubeConfig. " +
                            "Namespace hardcoded in plugin, and this check only prevents from using wrong context"
                    }

                    if (namespace == "default") {
                        require(
                            value = namespaceFromKubeConfig == "default" || namespaceFromKubeConfig.isNullOrBlank(),
                            lazyMessage = namespaceDiffErrorMessage
                        )
                    } else {
                        require(
                            value = namespaceFromKubeConfig == namespace,
                            lazyMessage = namespaceDiffErrorMessage
                        )
                    }

                    requestConfig.oauthTokenProvider = oauthTokenProvider(configFile)
                }
            }
            is KubernetesCredentials.Empty ->
                throw IllegalStateException("Can't create kubernetesClient without credentials")
        }

        val kubernetesHttpClient = HttpClientUtils.createHttpClient(config).newBuilder()
        val client = kubernetesHttpClient.eventListenerFactory {
            StatsHttpEventListener(
                statsDSender = statsDSender,
                timeProvider = timeProvider,
                requestMetadataProvider = KubernetesRequestMetadataProvider(),
                loggerFactory = loggerFactory,
            )
        }
            .addInterceptor(RetryInterceptor(tries = httpTries))
            .addInterceptor(HttpLoggingInterceptor(logger::info).apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()

        return DefaultKubernetesClient(client, config)
    }

    /**
     * apis_apps_v1_namespaces_android-emulator_deployments
     * OAuth token provider that automatically refreshes an expired token and persists changes to kube config.
     * https://kubernetes.io/docs/reference/access-authn-authz/authentication/#openid-connect-tokens
     */
    private fun oauthTokenProvider(config: File): OAuthTokenProvider {
        val kubeConfig = KubeConfig.loadKubeConfig(config.inputStream().reader())
        val persister = FilePersister(config)
        kubeConfig.setPersistConfig(persister)
        KubeConfig.registerAuthenticator(CustomGCPAuthenticator())
        return OAuthTokenProvider { kubeConfig.accessToken }
    }
}
