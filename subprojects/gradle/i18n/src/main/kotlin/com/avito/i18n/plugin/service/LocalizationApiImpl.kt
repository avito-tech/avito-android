package com.avito.i18n.plugin.service

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.tls.TlsCredentialsService
import com.avito.android.tls.manager.TlsManager
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

    override fun translateWithNewApi(request: TranslationRequest): Result<TranslationResponse> {
        return runCatching {
            runBlocking {
                val response = client.post {
                    url(path = translateUrlPath)
                    setBody(request)
                }
                if (response.status == HttpStatusCode.OK) {
                    response.body()
                } else {
                    throw Problem.Builder(
                        shortDescription = "Translation API request failed",
                        context = "POST ${response.request.url}"
                    )
                        .because("Server responded with HTTP ${response.status}")
                        .build()
                        .asRuntimeException()
                }
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
