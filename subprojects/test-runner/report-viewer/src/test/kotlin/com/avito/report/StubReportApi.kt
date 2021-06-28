package com.avito.report

import com.avito.http.HttpCodes
import com.avito.report.internal.model.RpcResult
import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.RequestCapturer
import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse

internal class StubReportApi(
    private val realApi: ReportsApi,
    private val mockDispatcher: MockDispatcher,
) {

    private val gson = Gson()

    fun addTest(reportCoordinates: ReportCoordinates, buildId: String?, test: AndroidTest): RequestCapturer.Checks {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { true },
                response = MockResponse()
                    .setResponseCode(HttpCodes.OK)
                    .setBody(gson.toJson(RpcResult("12345")))
            )
        )

        val request = mockDispatcher.captureRequest { true }.checks

        realApi.addTest(reportCoordinates, buildId, test).getOrThrow()

        return request
    }
}
