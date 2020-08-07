package com.avito.plugin

import com.avito.test.http.MockWebServerFactory
import com.avito.utils.logging.CILogger
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.funktionale.tries.Try
import org.funktionale.tries.Try.Failure
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class SignViaServiceActionTest {

    private lateinit var testProjectDir: File

    private val server = MockWebServerFactory.create()

    private val apk: File
        get() = File(testProjectDir, "test.apk").apply { createNewFile() }

    private val signViaServiceAction: SignViaServiceAction
        get() = SignViaServiceAction(
            server.url("/").toString(),
            "123456",
            apk,
            apk,
            CILogger.allToStdout
        )

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        testProjectDir = tempPath.toFile()
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private val failedResponse = MockResponse().setResponseCode(500)
    private val successResponse = MockResponse().setResponseCode(200)

    @Test
    fun `action failed - when http request failed all attempts`() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return failedResponse
            }
        }

        val result = signViaServiceAction.sign()

        assertThat(result).isInstanceOf(Try.Failure::class.java)
        assertThat((result as Failure).throwable.message).contains("Failed to sign apk via service")

        assertWithMessage("retry to send")
            .that(server.requestCount).isAtLeast(5)
    }

    @Test
    fun `action success - successful request after failed`() {
        server.enqueue(failedResponse)
        server.enqueue(successResponse)

        val result = signViaServiceAction.sign()

        assertThat(result).isInstanceOf(Try.Success::class.java)
        assertThat(server.requestCount).isEqualTo(2)
    }

    @Test
    fun `action success - request contains passed params`() {
        server.enqueue(successResponse)

        val result = signViaServiceAction.sign()

        assertThat(server.requestCount).isEqualTo(1)
        val recordedRequest = server.takeRequest()

        assertThat(recordedRequest.path).isEqualTo("/sign")
        assertThat(recordedRequest.body.readUtf8()).contains("12345")

        assertThat(result).isInstanceOf(Try.Success::class.java)
    }

}
