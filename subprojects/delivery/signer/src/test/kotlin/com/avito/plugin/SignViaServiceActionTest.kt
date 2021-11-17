package com.avito.plugin

import com.avito.android.Result
import com.avito.http.HttpClientProvider
import com.avito.http.createStubInstance
import com.avito.plugin.internal.SignViaServiceAction
import com.avito.test.http.MockWebServerFactory
import com.avito.truth.assertThat
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class SignViaServiceActionTest {

    private lateinit var testProjectDir: File

    private val server = MockWebServerFactory.create()

    private val httpClientProvider = HttpClientProvider.createStubInstance()

    private val apk: File
        get() = File(testProjectDir, "test.apk").apply { createNewFile() }

    private val signViaServiceAction: SignViaServiceAction
        get() = SignViaServiceAction(
            serviceUrl = server.url("/"),
            httpClient = httpClientProvider.provide().build(),
            token = "123456",
            unsignedFile = apk,
            signedFile = apk,
        )

    private val failedResponse = MockResponse().setResponseCode(500)

    private val successResponse = MockResponse().setResponseCode(200)

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        testProjectDir = tempPath.toFile()
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `action failed - when http request failed`() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return failedResponse
            }
        }

        val result = signViaServiceAction.sign()

        assertThat<Result.Failure<*>>(result) {
            assertThat(throwable.message).run {
                contains("Failed to sign $testProjectDir/test.apk via service")
                contains("Request: POST ${server.url("/")}")
                contains("Request body size: ")
                contains("Response: 500")
                contains("Response headers:")
                contains("Content-Length: 0")
                contains("Response body is empty")
            }
        }
    }

    @Test
    fun `action success - request contains passed params`() {
        server.enqueue(successResponse)

        val result = signViaServiceAction.sign()

        assertThat(server.requestCount).isEqualTo(1)
        val recordedRequest = server.takeRequest()

        assertThat(recordedRequest.path).isEqualTo("/")
        assertThat(recordedRequest.body.readUtf8()).contains("12345")

        assertThat(result).isInstanceOf<Result.Success<*>>()
    }
}
