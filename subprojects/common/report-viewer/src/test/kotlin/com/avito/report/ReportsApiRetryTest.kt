package com.avito.report

import com.avito.http.HttpClientProvider
import com.avito.http.createStubInstance
import com.avito.logger.StubLoggerFactory
import com.avito.report.model.GetReportResult
import com.avito.report.model.ReportCoordinates
import com.avito.test.http.MockWebServerFactory
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ReportsApiRetryTest {

    private val mockWebServer = MockWebServerFactory.create()

    private val loggerFactory = StubLoggerFactory

    private val retryCount = 5

    private lateinit var reportsApi: ReportsApi

    @BeforeEach
    fun setup() {
        mockWebServer.start()
        val host = mockWebServer.url("/").toString()
        reportsApi = ReportsApiFactory.create(
            host = host,
            loggerFactory = loggerFactory,
            httpClientProvider = HttpClientProvider.createStubInstance()
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

        assertThat(result).isInstanceOf<GetReportResult.Error>()
    }
}
