package com.avito.plugin

import com.avito.android.Result
import com.avito.http.HttpClientProvider
import com.avito.http.createStubInstance
import com.avito.logger.PrintlnLoggerFactory
import com.avito.test.http.MockWebServerFactory
import com.avito.truth.ResultSubject.Companion.assertThat
import com.avito.truth.assertThat
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class QAppsUploadActionTest {

    private lateinit var testProjectDir: File

    private val server = MockWebServerFactory.create()

    private val apk: File
        get() {
            val apk = File(testProjectDir, "test.apk")
            apk.createNewFile()
            apk.writeText("content")
            return apk
        }

    private val action: QAppsUploadAction
        get() = QAppsUploadAction(
            apk = apk,
            comment = "comment",
            host = server.url("/").toString(),
            branch = "develop",
            versionName = "version_name",
            versionCode = "0",
            packageName = "com.avito.android",
            releaseChain = false,
            httpClientProvider = HttpClientProvider.createStubInstance(),
            loggerFactory = PrintlnLoggerFactory
        )

    private val failedResponse = MockResponse()
        .setResponseCode(500)
        .setBody("[error reason]")

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
    fun `action failed - when http request failed all attempts`() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return failedResponse
            }
        }

        val result = action.upload()

        assertThat<Result.Failure<*>>(result) {
            assertThat(throwable.message).contains("Can't upload apk to qapps")
            assertThat(throwable.message).contains("[error reason]")
        }

        assertWithMessage("retry to send")
            .that(server.requestCount).isAtLeast(2)
    }

    @Test
    fun `action success - successful request after failed`() {
        server.enqueue(failedResponse)
        server.enqueue(successResponse)

        val result = action.upload()

        assertThat(result).isSuccess()
        assertThat(server.requestCount).isEqualTo(2)
    }

    @Test
    fun `action success - request contains passed params`() {
        server.enqueue(successResponse)

        val result = action.upload()

        assertThat(server.requestCount).isEqualTo(1)
        val recordedRequest = server.takeRequest()

        assertThat(recordedRequest.path).isEqualTo("/qapps/api/os/android/upload")
        assertThat(recordedRequest.body.readUtf8()).contains("content")

        assertThat(result).isSuccess()
    }
}
