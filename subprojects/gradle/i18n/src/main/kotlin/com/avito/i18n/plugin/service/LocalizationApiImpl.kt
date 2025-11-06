package com.avito.i18n.plugin.service

import com.avito.android.tls.TlsCredentialsService
import com.avito.android.tls.manager.TlsManager
import com.avito.i18n.plugin.dto.OldTranslationRequest
import com.avito.i18n.plugin.dto.OldTranslationResponse
import com.avito.i18n.plugin.dto.TranslationRequest
import com.avito.i18n.plugin.dto.TranslationResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.gradle.api.GradleException
import java.util.concurrent.TimeUnit

internal class LocalizationApiImpl(
    private val serviceUrl: String,
    private val tlsCredentialsService: TlsCredentialsService,
    private val translateUrlPath: String,
    private val useTls: Boolean
) : LocalizationApi {

    private val client: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    explicitNulls = false
                    ignoreUnknownKeys = true
                }
            )
        }
        install(HttpTimeout) {
            socketTimeoutMillis = DEFAULT_TIMEOUT_MILLIS
        }
        defaultRequest {
            url(serviceUrl)
            contentType(ContentType.Application.Json)
        }
        engine {
            preconfigured = prepareDefaultHttpClient().build()
        }
    }

    override fun translateWithDeprecatedApi(request: OldTranslationRequest): OldTranslationResponse {
        return runBlocking {
            val response = client.post {
                url(path = translateUrlPath)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body()
            } else {
                throw GradleException("Error while processing request:  <-- ${response.status} ${response.request.url}")
            }
        }
    }

    override fun translateWithNewApi(request: TranslationRequest): TranslationResponse {
        return runBlocking {
            val response = client.post {
                url(path = translateUrlPath)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body()
            } else {
                throw GradleException("Error while processing request:  <-- ${response.status} ${response.request.url}")
            }
        }
    }

    private fun prepareDefaultHttpClient(): OkHttpClient.Builder {
        return OkHttpClient.Builder().apply {
            if (useTls) {
                val tlsManager = TlsManager(tlsCredentialsService.createCredentials())
                val handshakeCertificates = tlsManager.handshakeCertificates()
                sslSocketFactory(handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager)
            }
        }
    }

    companion object {
        private val DEFAULT_TIMEOUT_MILLIS: Long = TimeUnit.MINUTES.toMillis(15)
    }
}
