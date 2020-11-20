package com.avito.test.http

import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.Collections

class MockDispatcher(
    private val unmockedResponse: MockResponse = MockResponse().setResponseCode(418).setBody("Not mocked"),
    private val logger: (String) -> Unit = {}
) : Dispatcher() {

    private val mocks = Collections.synchronizedList(mutableListOf<Mock>())

    private val capturers = Collections.synchronizedList(mutableListOf<RequestCapturer>())

    fun registerMock(mock: Mock) {
        mocks.add(mock)
    }

    fun captureRequest(requestMatcher: RequestData.() -> Boolean): RequestCapturer {
        val capturer = RequestCapturer(requestMatcher)
        capturers.add(capturer)
        return capturer
    }

    override fun dispatch(request: RecordedRequest): MockResponse {

        val requestData = RequestData(request)

        synchronized(capturers) {
            capturers.find { it.requestMatcher.invoke(requestData) }?.run {
                capture(request)
                logger("request captured: $request")
            }
        }

        /**
         * to be able to replace mocks (last one wins) (see MBS-5878)
         *
         * synchronized because of concurrent iterator access (see MBS-7636)
         */
        val matchedMock = synchronized(mocks) {
            mocks.findLast { it.requestMatcher.invoke(requestData) }
        }
        val response = matchedMock?.response ?: unmockedResponse

        if (matchedMock?.removeAfterMatched == true) mocks.remove(matchedMock)

        logger.invoke("got request: ${request.path}, response will be: $response")

        return response
    }
}

private val gson = Gson()

/**
 * Use file contents as response body
 *
 * @param fileName specify file path, relative to assets dir
 *                 example: "assets/mock/seller_x/publish/parameters/ok.json"
 */
fun MockResponse.setBodyFromFile(fileName: String): MockResponse {
    val text = textFromAsset<MockDispatcher>(fileName)
    requireNotNull(text) { "$fileName not found, check path" }
    if (fileName.endsWith(".json")) {
        val exception = validateJson(text)
        if (exception != null) {
            throw IllegalArgumentException("$fileName contains invalid json", exception)
        }
    }
    setBody(text)
    return this
}

private fun validateJson(json: String): Throwable? {
    return try {
        gson.fromJson(json, JsonElement::class.java)
        null
    } catch (t: Throwable) {
        t
    }
}

private inline fun <reified C> textFromAsset(fileName: String): String? {
    return C::class.java.classLoader?.getResource(fileName)?.readText()
}
