package com.avito.http

import com.avito.logger.StubLogger
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class RetryWithFallbackTest {

    private val logger = StubLogger
    private val mockDispatcher = MockDispatcher()
    private val server = MockWebServerFactory.create().apply { dispatcher = mockDispatcher }

    @Test
    fun `retry with fallback`() {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path == "/" },
                response = MockResponse().setResponseCode(503)
            )
        )

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("fallback") },
                response = MockResponse().setBody("ok").setResponseCode(200)
            )
        )

        val api = createApi(server.url("/")) {
            addInterceptor(
                RetryInterceptor(
                    allowedMethods = listOf("POST"),
                    allowedCodes = listOf(503),
                    logger = logger
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
                    doFallbackOnTheseCodes = listOf(503),
                    logger = logger
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
