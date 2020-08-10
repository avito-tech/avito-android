package com.avito.report

import com.avito.logger.NoOpLogger
import com.avito.test.gradle.fileFromJarResources
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.funktionale.tries.Try
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class ReportsFetchApiImplTest {

    private val mockDispatcher = MockDispatcher()
    private val mockWebServer = MockWebServerFactory.create().apply { dispatcher = mockDispatcher }

    private val fetchApi: ReportsFetchApi = ReportsApi.create(
        host = mockWebServer.url("/").toString(),
        fallbackUrl = "",
        logger = NoOpLogger
    )

    @Test
    fun `getPerformanceTest - returns ok`() {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { true },
                response = MockResponse().setBody(fileFromJarResources<ReportsApiTest>("getTest.json").readText())
            )
        )

        val result = fetchApi.getPerformanceTest("any")

        result.onFailure {
            it.printStackTrace()
        }

        Truth.assertThat(result).isInstanceOf(Try.Success::class.java)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }
}
