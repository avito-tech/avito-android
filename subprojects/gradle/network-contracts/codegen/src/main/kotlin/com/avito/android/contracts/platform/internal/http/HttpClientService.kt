package com.avito.android.contracts.platform.internal.http

import com.avito.android.contracts.platform.extension.ContractsRootExtension
import com.avito.android.contracts.platform.extension.configurations.network.Timeouts
import com.avito.android.contracts.platform.extension.defaultNetwork
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.android.tls.TlsCredentialsService
import com.avito.android.tls.manager.TlsManager
import com.avito.logger.Logger
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.getByType

public abstract class HttpClientService : BuildService<HttpClientService.Params> {

    private val predefinedOkHttpBuilder: OkHttpClient.Builder by lazy {
        prepareDefaultHttpClient()
    }

    public interface Params : BuildServiceParameters {
        public val serviceUrl: Property<String>
        public val serviceName: Property<String>
        public val tlsCredentialsService: Property<TlsCredentialsService>
        public val useTls: Property<Boolean>
        public val timeouts: Property<Timeouts>
        public val retries: Property<Int>
    }

    public fun buildClient(
        logger: Logger? = null,
        builder: HttpClientConfig<OkHttpConfig>.() -> Unit = {}
    ): HttpClient = with(parameters) {
        val okHttpClientBuilder = predefinedOkHttpBuilder.apply {
            if (logger != null) {
                addInterceptor(
                    HttpLoggingInterceptor(logger::info).setLevel(HttpLoggingInterceptor.Level.BASIC)
                )
            }
            if (parameters.timeouts.isPresent) {
                val timeouts = parameters.timeouts.get()
                connectTimeout(timeouts.connectTimeout)
                readTimeout(timeouts.readTimeout)
                writeTimeout(timeouts.writeTimeout)
            }
        }

        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }
            if (retries.isPresent && retries.get() > 0) {
                install(HttpRequestRetry) {
                    maxRetries = retries.get()
                    retryIf { _, httpResponse ->
                        httpResponse.status in setOf(
                            HttpStatusCode.BadGateway,
                            HttpStatusCode.GatewayTimeout,
                            HttpStatusCode.RequestTimeout,
                            HttpStatusCode.ServiceUnavailable,
                        )
                    }
                    retryOnExceptionIf { _, throwable ->
                        throwable is HttpRequestTimeoutException ||
                            throwable is ConnectTimeoutException ||
                            throwable is SocketTimeoutException
                    }
                    exponentialDelay()
                }
            }

            defaultRequest {
                url {
                    takeFrom(serviceUrl.get())
                    serviceName.orNull?.let { path("$it/") }
                }
            }
            builder.invoke(this)

            engine {
                preconfigured = okHttpClientBuilder.build()
            }
        }
    }

    private fun prepareDefaultHttpClient(): OkHttpClient.Builder {
        return OkHttpClient.Builder().apply {
            if (parameters.useTls.get()) {
                val tlsManager = TlsManager(parameters.tlsCredentialsService.get().createCredentials())
                val handshakeCertificates = tlsManager.handshakeCertificates()
                sslSocketFactory(handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager)
            }
        }
    }

    public companion object {
        public fun provideHttpClientService(
            project: Project,
        ): Provider<HttpClientService> {
            return registerService(project)
        }

        public fun provideHttpClientService(
            project: Project,
            variantName: String,
        ): Provider<HttpClientService> {
            return registerService(project, variantName)
        }

        private fun registerService(
            project: Project,
            variantName: String? = null,
        ): Provider<HttpClientService> {
            return project.gradle.sharedServices.registerIfAbsent(
                HttpClientService::class.java.name,
                HttpClientService::class.java,
            ) { service ->
                val rootExtension = project.rootProject.extensions.getByType<ContractsRootExtension>()
                val networkConfiguration = variantName?.let { rootExtension.networks.findByName(variantName) }
                    ?: rootExtension.defaultNetwork

                service.parameters { params ->
                    params.serviceUrl.set(networkConfiguration.serviceUrl)
                    params.serviceName.set(networkConfiguration.serviceName)
                    params.tlsCredentialsService.set(TlsConfigurationPlugin.provideCredentialsService(project))
                    params.useTls.set(networkConfiguration.useTls)
                    params.timeouts.set(networkConfiguration.timeouts)
                    params.retries.set(networkConfiguration.retries)
                }
            }
        }
    }
}

public fun HttpClientService.buildClientWithBaseUrl(baseUrl: Provider<String>): HttpClient {
    return buildClient {
        defaultRequest {
            url {
                val baseUrl = baseUrl.orNull
                if (!baseUrl.isNullOrBlank()) {
                    takeFrom(baseUrl)
                }
            }
        }
    }
}
