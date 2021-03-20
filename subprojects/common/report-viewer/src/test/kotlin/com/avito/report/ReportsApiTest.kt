package com.avito.report

import com.avito.logger.StubLoggerFactory
import com.avito.report.model.GetReportResult
import com.avito.report.model.ReportCoordinates
import com.avito.test.http.MockWebServerFactory
import com.avito.truth.ResultSubject.Companion.assertThat
import com.avito.truth.isInstanceOf
import com.avito.utils.fileFromJarResources
import com.github.salomonbrys.kotson.jsonObject
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class ReportsApiTest {

    private val mockWebServer = MockWebServerFactory.create()

    private val loggerFactory = StubLoggerFactory

    @Test
    fun `getReport - returns NotFound - when throws exception with no data`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody(
                    "{\"jsonrpc\":\"2.0\"," +
                        "\"error\":{\"code\":-32603,\"message\":\"Internal error\",\"data\":\"not found\"}," +
                        "\"id\":1}"
                )
        )

        val result = createNoRetriesReportsApi().getReport(
            ReportCoordinates("AvitoAndroid", "FunctionalTests", "12345")
        )

        assertThat(result).isInstanceOf<GetReportResult.NotFound>()
    }

    @Test
    fun `getReport - returns Error - when throws exception with no data`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result = createNoRetriesReportsApi().getReport(
            ReportCoordinates("AvitoAndroid", "FunctionalTests", "12345")
        )

        assertThat(result).isInstanceOf<GetReportResult.Error>()
    }

    @Test
    fun `getReport - returns Report`() {
        mockWebServer.enqueue(
            MockResponse()
                .setBody(fileFromJarResources<ReportsApiTest>("getReport.json").readText())
        )

        val result = createNoRetriesReportsApi().getReport(ReportCoordinates("AvitoAndroid", "FunctionalTests", ""))

        assertThat(result).isInstanceOf<GetReportResult.Found>()

        (result as GetReportResult.Found).report.run {
            // see json
            assertThat(id).isEqualTo("5c8032d5ccdf780001c49576")
        }
    }

    @Test
    fun `getTestsForRunId - returns ok`() {
        mockWebServer.enqueue(
            MockResponse().setBody(fileFromJarResources<ReportsApiTest>("getReport.json").readText())
        )
        mockWebServer.enqueue(
            MockResponse().setBody(fileFromJarResources<ReportsApiTest>("getTestsForRunId.json").readText())
        )

        val result = createNoRetriesReportsApi().getTestsForRunId(
            ReportCoordinates("AvitoAndroid", "FunctionalTests", "")
        )

        assertThat(result).isSuccess()

        assertThat(
            result.getOrThrow().first().name
        ).isEqualTo("ru.domofond.features.RemoteToggleMonitorTest.check_remote_toggle")
    }

    @Test
    fun `pushPreparedData - returns ok`() {
        mockWebServer.enqueue(
            MockResponse().setBody(fileFromJarResources<ReportsApiTest>("pushPreparedData.json").readText())
        )

        val result = createNoRetriesReportsApi().pushPreparedData("any", "any", jsonObject("any" to "any"))

        assertThat(result).isSuccess()
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    private fun createNoRetriesReportsApi(): ReportsApi = ReportsApiFactory.create(
        host = mockWebServer.url("/").toString(),
        loggerFactory = loggerFactory,
        retryInterceptor = null
    )
}
