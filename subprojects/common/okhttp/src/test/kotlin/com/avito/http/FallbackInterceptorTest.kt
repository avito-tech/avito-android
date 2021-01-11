package com.avito.http

import com.avito.logger.StubLogger
import com.avito.logger.StubLoggerFactory
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class FallbackInterceptorTest {

    private val loggerFactory = StubLoggerFactory
    private val mockDispatcher = MockDispatcher(loggerFactory = loggerFactory)
    private val server: MockWebServer = MockWebServerFactory.create().apply { dispatcher = mockDispatcher }

    private val doFallbackOnThisResponseCode = 503

    private val api: StubApi by lazy {
        createApi(
            baseUrl = server.url("/")
        ) {
            addInterceptor(
                FallbackInterceptor(
                    fallbackRequest = { request ->
                        request.newBuilder()
                            .url(request.url.newBuilder().addPathSegment("fallback").build())
                            .addHeader("X-FALLBACK", "true")
                            .build()
                    },
                    doFallbackOnTheseCodes = listOf(doFallbackOnThisResponseCode),
                    logger = StubLogger
                )
            )
        }
    }

    @Test
    fun `request success - response is successful`() {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { true },
                response = MockResponse().setResponseCode(200)
            )
        )

        val result = api.request().execute()

        assertThat(result.isSuccessful).isTrue()
        assertThat(server.requestCount).isEqualTo(1)
    }

    @Test
    fun `request failed - fallback is successful`() {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path == "/" },
                response = MockResponse().setResponseCode(doFallbackOnThisResponseCode)
            )
        )

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path == "/fallback" },
                response = MockResponse().setResponseCode(200)
            )
        )

        val fallbackRequest = mockDispatcher.captureRequest { path.contains("fallback") }

        val result = api.request().execute()

        fallbackRequest.checks.singleRequestCaptured().containsHeader("X-FALLBACK", "true")

        assertThat(result.isSuccessful).isTrue()
        assertThat(server.requestCount).isEqualTo(2)
    }

    @Test
    fun `request failed - fallback is failed`() {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path == "/" },
                response = MockResponse().setResponseCode(doFallbackOnThisResponseCode)
            )
        )

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path == "/fallback" },
                response = MockResponse().setResponseCode(doFallbackOnThisResponseCode)
            )
        )

        val fallbackRequest = mockDispatcher.captureRequest { path.contains("fallback") }

        val result = api.request().execute()

        fallbackRequest.checks.singleRequestCaptured().containsHeader("X-FALLBACK", "true")

        assertThat(result.isSuccessful).isFalse()
        assertThat(server.requestCount).isEqualTo(2)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }
}
