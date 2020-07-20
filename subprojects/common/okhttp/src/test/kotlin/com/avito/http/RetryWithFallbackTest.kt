package com.avito.http

import com.avito.logger.FakeLogger
import com.avito.test.http.MockDispatcher
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class RetryWithFallbackTest {

    private val mockDispatcher = MockDispatcher()
    private val server = MockWebServer().apply { dispatcher = mockDispatcher }

    @Test
    fun `retry with fallback`() {
        mockDispatcher.mockResponse(
            requestMatcher = { path == "/" },
            response = MockResponse().setResponseCode(503)
        )

        mockDispatcher.mockResponse(
            requestMatcher = { path?.contains("fallback") ?: false },
            response = MockResponse().setBody("ok").setResponseCode(200)
        )

        val api = createApi(server.url("/")) {
            addInterceptor(
                RetryInterceptor(
                    allowedMethods = listOf("POST"),
                    allowedCodes = listOf(503),
                    logger = FakeLogger
                )
            )
            addInterceptor(
                FallbackInterceptor(
                    fallbackRequest = { request ->
                        request.newBuilder()
                            .url(
                                request.url
                                    .newBuilder()
                                    .encodedPath("/fallback")
                                    .build()
                            ).build()
                    },
                    doFallbackOnTheseCodes = listOf(503)
                )
            )
        }

        val response = api.request().execute()

        assertThat(response.isSuccessful).isTrue()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }
}
