package com.avito.android.mock

import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.rule.SimpleRule
import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.RequestCapturer
import com.avito.test.http.RequestData
import okhttp3.mockwebserver.MockWebServer

class MockWebServerApiRule : SimpleRule() {

    private val mockWebServer: MockWebServer
        get() = (InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner).mockWebServer

    private val mockDispatcher: MockDispatcher
        get() = (InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner).mockDispatcher

    override fun after() {
        mockWebServer.shutdown()
    }

    fun registerMock(mock: Mock) {
        mockDispatcher.registerMock(mock)
    }

    fun captureRequest(requestMatcher: RequestData.() -> Boolean): RequestCapturer {
        return mockDispatcher.captureRequest(requestMatcher)
    }
}
