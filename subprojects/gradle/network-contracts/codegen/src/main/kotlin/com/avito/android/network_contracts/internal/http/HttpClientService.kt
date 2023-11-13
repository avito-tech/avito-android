package com.avito.android.network_contracts.internal.http

import com.avito.android.network_contracts.shared.networkContractsRootExtension
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.android.tls.TlsCredentialsService
import com.avito.android.tls.manager.TlsManager
import com.avito.logger.Logger
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

internal abstract class HttpClientService : BuildService<HttpClientService.Params> {

    internal interface Params : BuildServiceParameters {
        val serviceUrl: Property<String>
        val tlsCredentialsService: Property<TlsCredentialsService>
        val useTls: Property<Boolean>
    }

    internal fun buildClient(
        logger: Logger? = null,
        builder: HttpClientConfig<OkHttpConfig>.() -> Unit = {}
    ): HttpClient = with(parameters) {
        val okHttpClientBuilder = OkHttpClient.Builder().apply {
            if (logger != null) {
                addInterceptor(
                    HttpLoggingInterceptor(logger::info).setLevel(HttpLoggingInterceptor.Level.BASIC)
                )
            }

            if (useTls.get()) {
                val tlsManager = TlsManager(tlsCredentialsService.get().createCredentials())
                val handshakeCertificates = tlsManager.handshakeCertificates()
                sslSocketFactory(handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager)
            }
        }

        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }
            defaultRequest { url(serviceUrl.get()) }
            builder.invoke(this)

            engine {
                preconfigured = okHttpClientBuilder.build()
            }
        }
    }

    companion object {
        internal fun provideHttpClientService(
            project: Project,
        ): Provider<HttpClientService> {
            return registerService(project)
        }

        private fun registerService(project: Project): Provider<HttpClientService> {
            return project.gradle.sharedServices.registerIfAbsent(
                HttpClientService::class.java.name,
                HttpClientService::class.java,
            ) {
                val networkContractsRootExtension = project.networkContractsRootExtension
                it.parameters {
                    it.serviceUrl.set(networkContractsRootExtension.serviceUrl)
                    it.tlsCredentialsService.set(TlsConfigurationPlugin.provideCredentialsService(project))
                    it.useTls.set(networkContractsRootExtension.useTls)
                }
            }
        }
    }
}
