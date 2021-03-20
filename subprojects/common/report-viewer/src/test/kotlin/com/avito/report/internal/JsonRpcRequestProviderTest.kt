package com.avito.report.internal

import com.avito.logger.StubLoggerFactory
import com.avito.report.ReportsApiFactory.describeJsonRpc
import com.avito.report.internal.model.RfcRpcRequest
import com.avito.test.http.MockDispatcher
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class JsonRpcRequestProviderTest {

    private val mockWebServer = MockWebServer()

    private val loggerFactory = StubLoggerFactory

    init {
        MockDispatcher(
            unmockedResponse = MockResponse().setResponseCode(200).setBody("{}"),
            loggerFactory = loggerFactory
        )
            .also { dispatcher -> mockWebServer.dispatcher = dispatcher }
    }

    @Test
    fun `describe request - contains method tag`() {
        val provider = createProvider(requestAssertion = { assertThat(it.describeJsonRpc()).contains("Some.Method") })
        provider.jsonRpcRequest<Unit>(
            RfcRpcRequest(
                method = "Some.Method",
                params = emptyMap()
            )
        )
    }

    @Test
    fun `describe batch request - contains method tag`() {
        val provider = createProvider(requestAssertion = { assertThat(it.describeJsonRpc()).contains("Some.Method") })
        provider.batchRequest<Unit>(
            listOf(
                RfcRpcRequest(
                    method = "Some.Method",
                    params = emptyMap()
                )
            )
        )
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    private fun createProvider(requestAssertion: (Request) -> Unit): JsonRpcRequestProvider {
        return JsonRpcRequestProvider(
            host = mockWebServer.url("/").toString(),
            httpClient = OkHttpClient.Builder()
                .addInterceptor(CheckTagInterceptor(requestAssertion))
                .build(),
            gson = Gson()
        )
    }

    private class CheckTagInterceptor(private val requestAssertion: (Request) -> Unit) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            requestAssertion.invoke(chain.request())
            return chain.proceed(chain.request())
        }
    }
}
