package com.avito.http

import com.avito.logger.NoOpLogger
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.funktionale.tries.Try
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.io.IOException

internal class RetryInterceptorTest {

    private lateinit var server: MockWebServer

    private val errorResponseCases = mapOf<String, MockResponse>(
        "500" to MockResponse().setResponseCode(500),
        "DISCONNECT_AT_START" to MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
    )

    private val successfulResponse = MockResponse()

    @BeforeEach
    private fun setup() {
        server = MockWebServerFactory.create() // can't reuse same server in dynamic tests after shutdown
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `request success - response is successful`() {
        server.enqueue(MockResponse().setResponseCode(200))

        val result = createApi().request().execute()

        assertThat(result.isSuccessful).isTrue()
        assertThat(server.requestCount).isEqualTo(1)
    }

    @TestFactory
    fun `request is successful - successful response after failed`(): List<DynamicTest> {
        return errorResponseCases.map { case ->
            dynamicTest(case.key) {
                setup()

                val error = case.value
                repeat(maxAttempts - 1) {
                    server.enqueue(error)
                }
                server.enqueue(successfulResponse)

                val response = makeRequest()

                assertThat(response.isSuccess()).isTrue()
                assertThat(response.get().isSuccessful).isTrue()
                assertThat(server.requestCount).isEqualTo(maxAttempts)

                tearDown()
            }
        }
    }

    @TestFactory
    fun `request failed - all http request failed`(): List<DynamicTest> {
        return errorResponseCases.map { case ->
            dynamicTest(case.key) {
                setup()

                val error = case.value
                repeat(maxAttempts) {
                    server.enqueue(error)
                }

                val response = makeRequest()

                assertThat(server.requestCount).isEqualTo(maxAttempts)
                response.fold(
                    {
                        assertThat(it.isSuccessful).isFalse()
                    },
                    {
                        assertThat(it).isInstanceOf(IOException::class.java)
                    }
                )
                tearDown()
            }
        }
    }

    private fun createApi(): FakeApi {
        return createApi(baseUrl = server.url("/")) {
            addInterceptor(
                RetryInterceptor(
                    retries = maxAttempts,
                    delayMs = 1,
                    logger = NoOpLogger,
                    allowedMethods = listOf("GET", "POST"),
                    useIncreasingDelay = false
                )
            )
        }
    }

    private fun makeRequest() = Try {
        createApi().request().execute()
    }
}

private const val maxAttempts = 2
