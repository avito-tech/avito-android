package com.avito.reportviewer

import com.avito.http.HttpClientProvider
import com.avito.http.createStubInstance
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.http.MockWebServerFactory
import com.avito.test.model.TestName
import com.avito.truth.ResultSubject.Companion.assertThat
import com.avito.utils.ResourcesReader
import com.github.salomonbrys.kotson.jsonObject
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class ReportsApiTest {

    private val mockWebServer = MockWebServerFactory.create()

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

        assertThat(result).isFailure()
    }

    @Test
    fun `getReport - returns Error - when throws exception with no data`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result = createNoRetriesReportsApi().getReport(
            ReportCoordinates("AvitoAndroid", "FunctionalTests", "12345")
        )

        assertThat(result).isFailure()
    }

    @Test
    fun `getReport - returns Report`() {
        mockWebServer.enqueue(
            MockResponse()
                .setBody(ResourcesReader.readText("getReport.json"))
        )

        val result = createNoRetriesReportsApi().getReport(ReportCoordinates("AvitoAndroid", "FunctionalTests", ""))

        assertThat(result).isSuccess().withValue {
            assertThat(it.id).isEqualTo("5c8032d5ccdf780001c49576")
        }
    }

    @Test
    fun `getTestsForRunId - returns ok`() {
        mockWebServer.enqueue(
            MockResponse().setBody(ResourcesReader.readText("getReport.json"))
        )
        mockWebServer.enqueue(
            MockResponse().setBody(ResourcesReader.readText("getTestsForRunId.json"))
        )

        val result = createNoRetriesReportsApi().getTestsForRunId(
            ReportCoordinates("AvitoAndroid", "FunctionalTests", "")
        )

        assertThat(result).isSuccess().withValue {
            assertThat(it.first().name).isEqualTo(
                TestName(
                    "ru.domofond.features.RemoteToggleMonitorTest",
                    "check_remote_toggle"
                )
            )
        }
    }

    @Test
    fun `pushPreparedData - returns ok`() {
        mockWebServer.enqueue(
            MockResponse().setBody(ResourcesReader.readText("pushPreparedData.json"))
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
        httpClientProvider = HttpClientProvider.createStubInstance(),
        retryRequests = false
    )
}
