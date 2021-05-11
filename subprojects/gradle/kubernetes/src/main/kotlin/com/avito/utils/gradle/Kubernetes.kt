@file:JvmName("Kubernetes")

package com.avito.utils.gradle

import com.avito.android.Result
import com.avito.http.HttpClientProvider
import com.avito.http.internal.RequestMetadata
import com.avito.http.internal.RequestMetadataProvider
import com.avito.kotlin.dsl.getOptionalStringProperty
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.OAuthTokenProvider
import io.fabric8.kubernetes.client.utils.HttpClientUtils
import io.kubernetes.client.util.FilePersister
import io.kubernetes.client.util.KubeConfig
import okhttp3.Request
import org.gradle.api.Project
import java.io.File

fun createKubernetesClient(
    httpClientProvider: HttpClientProvider,
    kubernetesCredentials: KubernetesCredentials,
    namespace: String
): KubernetesClient {
    val config = when (kubernetesCredentials) {
        is KubernetesCredentials.Service -> ConfigBuilder()
            .withCaCertData(kubernetesCredentials.caCertData)
            .withMasterUrl(kubernetesCredentials.url)
            .withOauthToken(kubernetesCredentials.token)
            .withNamespace(namespace)
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
                    "kubernetes.context.namespace should be $namespace, but was $namespaceFromKubeConfig. " +
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

    val httpClient = httpClientProvider
        .modifyExisting(
            builder = kubernetesHttpClient,
            requestMetadataProvider = KubernetesRequestMetadataProvider()
        )
        .build()

    return DefaultKubernetesClient(httpClient, config)
}

private class KubernetesRequestMetadataProvider : RequestMetadataProvider {

    override fun provide(request: Request): Result<RequestMetadata> {
        // example: apis_apps_v1_namespaces_android-emulator_deployments
        // drop apis_
        val methodName = request.url()
            .pathSegments()
            .drop(1)
            .joinToString(separator = "_")

        return Result.Success(RequestMetadata("k8s", methodName))
    }
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

val Project.kubernetesCredentials: KubernetesCredentials
    get() {
        val context = getOptionalStringProperty("kubernetesContext", nullIfBlank = true)
        return if (context.isNullOrBlank()) {
            val token = getOptionalStringProperty("kubernetesToken", nullIfBlank = true)
            val caCertData = getOptionalStringProperty("kubernetesCaCertData", nullIfBlank = true)
            val url = getOptionalStringProperty("kubernetesUrl", nullIfBlank = true)
            if (token != null && caCertData != null && url != null) {
                KubernetesCredentials.Service(
                    token = token,
                    caCertData = caCertData,
                    url = url
                )
            } else {
                KubernetesCredentials.Empty
            }
        } else {
            val caCertFile = getOptionalStringProperty("kubernetesCaCertFile", nullIfBlank = true)?.let {
                File(it)
            }
            KubernetesCredentials.Config(context, caCertFile = caCertFile)
        }
    }
