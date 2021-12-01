package com.avito.reportviewer

import com.avito.http.HttpClientProvider
import com.avito.http.createStubInstance
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.http.MockWebServerFactory
import com.avito.truth.ResultSubject.Companion.assertThat
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

        assertThat(result).isFailure()
    }
}
