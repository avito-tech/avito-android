package com.avito.reportviewer

import com.avito.logger.PrintlnLoggerFactory
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.http.MockWebServerFactory
import com.avito.truth.ResultSubject.Companion.assertThat
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ReportsApiRetryTest {

    private val mockWebServer = MockWebServerFactory.create()

    private val retryCount = 5

    private lateinit var reportsApi: ReportsApi

    @BeforeEach
    fun setup() {
        mockWebServer.start()
        val host = mockWebServer.url("/").toString()
        reportsApi = ReportsApiFactory.create(
            host = host,
            builder = OkHttpClient.Builder(),
            loggerFactory = PrintlnLoggerFactory,
        )
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getReport - retries - on error`() {
        repeat(retryCount) {
            mockWebServer.enqueue(MockResponse().setResponseCode(502))
        }

        val result = reportsApi.getReport(
            ReportCoordinates("AvitoAndroid", "FunctionalTests", "12345")
        )

        assertThat(result).isFailure()
    }
}
