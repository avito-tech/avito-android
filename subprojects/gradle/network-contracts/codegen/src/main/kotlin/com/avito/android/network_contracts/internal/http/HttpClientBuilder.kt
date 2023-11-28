package com.avito.android.network_contracts.internal.http

import com.avito.android.tls.TlsProjectCredentialsFactory
import com.avito.android.tls.manager.TlsManager
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
import org.gradle.api.provider.Provider

internal class HttpClientBuilder(
    private val configuration: HttpClientConfiguration
) {

    internal fun buildClient(
        builder: HttpClientConfig<OkHttpConfig>.() -> Unit = {}
    ): HttpClient = with(configuration) {
        val logger = loggerFactory.get().create("OkHttp")
        val okHttpClientBuilder = OkHttpClient.Builder().apply {
            addInterceptor(
                HttpLoggingInterceptor(logger::info).setLevel(HttpLoggingInterceptor.Level.BASIC)
            )

            if (tlsManager.isPresent) {
                val tlsManager = tlsManager.get()
                val handshakeCertificates = tlsManager.handshakeCertificates()
                sslSocketFactory(handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager)
            }
        }

        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                url(serviceUrl.get())
            }
            engine {
                preconfigured = okHttpClientBuilder.build()
            }
            builder.invoke(this)
        }
    }
}

internal fun Project.provideHttpClientBuilder(
    builder: HttpClientConfiguration.() -> Unit = {}
): HttpClientBuilder {
    val configuration = objects.newInstance(HttpClientConfiguration::class.java)
    builder.invoke(configuration)
    return HttpClientBuilder(configuration)
}

internal fun Project.provideTlsManager(useTls: Provider<Boolean>): Provider<TlsManager> {
    return provider {
        if (useTls.get()) {
            TlsManager(TlsProjectCredentialsFactory.createInstance(project))
        } else {
            null
        }
    }
}
