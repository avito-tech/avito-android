package com.avito.android.mock

import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.Result
import com.avito.android.rule.SimpleRule
import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.RequestCapturer
import com.avito.test.http.RequestData
import com.avito.utils.ResourcesReader
import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.mockwebserver.MockResponse
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

/**
 * Use file contents as response body
 *
 * @param fileName specify file path, relative to assets dir
 *                 example: "assets/mock/seller_x/publish/parameters/ok.json"
 */
fun MockResponse.setBodyFromFile(fileName: String): MockResponse = apply {
    val gson = (InstrumentationRegistry.getInstrumentation() as InHouseInstrumentationTestRunner).gson
    val text = ResourcesReader.readText(fileName)
    if (fileName.endsWith(".json")) {
        validateJson(text, gson).onFailure {
            throw IllegalArgumentException("$fileName contains invalid json", it)
        }
    }
    setBody(text)
}

private fun validateJson(json: String, gson: Gson): Result<Unit> =
    Result.tryCatch { gson.fromJson(json, JsonElement::class.java) }
