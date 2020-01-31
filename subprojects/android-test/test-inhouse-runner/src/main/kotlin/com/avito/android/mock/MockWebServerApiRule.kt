package com.avito.android.mock

import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.rule.SimpleRule
import com.avito.android.runner.InHouseInstrumentationTestRunner
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class MockWebServerApiRule : SimpleRule() {

    private val mockWebServer: MockWebServer
        get() = (InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner).mockWebServer

    private val mockDispatcher: MockDispatcher
        get() = (InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner).mockDispatcher

    override fun after() {
        mockWebServer.shutdown()
    }

    fun registerMock(mock: Mock) {
        mockDispatcher.mocks.add(mock)
    }

    fun captureRequest(requestMatcher: RequestData.() -> Boolean): RequestCapturer {
        val capturer = RequestCapturer(requestMatcher)
        mockDispatcher.capturers.add(capturer)
        return capturer
    }
}
