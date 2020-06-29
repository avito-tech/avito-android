package com.avito.android.mock

import android.annotation.SuppressLint
import android.util.Log
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.RequestCapturer
import com.avito.test.http.RequestData
import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.net.URL

@Deprecated(
    "use same class from common module",
    replaceWith = ReplaceWith("com.avito.test.http.MockDispatcher")
)
class MockDispatcher(
    private val unmockedResponse: MockResponse = MockResponse().setResponseCode(418).setBody("Not mocked"),
    private val logger: (String) -> Unit
) : Dispatcher() {

    internal val mocks = mutableListOf<Mock>()

    internal val capturers = mutableListOf<RequestCapturer>()

    @SuppressLint("LogNotTimber")
    override fun dispatch(request: RecordedRequest): MockResponse {

        capturers.find { it.requestMatcher.invoke(RequestData(request)) }?.run {
            capture(request)
            Log.d(TAG, "request captured: $request")
        }

        /**
         * last - чтобы иметь возможность перезаписывать моки, последний записанный должен выигрывать
         *
         * @see "MBS-5878"
         */
        val matchedMock = mocks.findLast { it.requestMatcher.invoke(RequestData(request)) }
        val response = matchedMock?.response ?: unmockedResponse

        if (matchedMock?.removeAfterMatched == true) mocks.remove(matchedMock)

        logger.invoke("got request: ${request.path}, response will be: $response")

        return response
    }
}

private val gson = Gson()

/**
 * Содержимое файла как тело запроса
 *
 * @param fileName указать относительный путь до файла, начиная с директории assets
 *                 например: "assets/mock/seller_x/publish/parameters/ok.json"
 */
@Deprecated(
    "use same class from common module",
    replaceWith = ReplaceWith("com.avito.test.http.setBodyFromFile")
)
fun MockResponse.setBodyFromFile(fileName: String): MockResponse {
    val text = textFromAsset<MockDispatcher>(fileName)
    requireNotNull(text) { "$fileName not found, check path" }
    if (fileName.endsWith(".json")) {
        val exception = validateJson(text)
        if (exception != null) {
            throw  IllegalArgumentException("$fileName contains invalid json", exception)
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

@SuppressLint("LogNotTimber")
private inline fun <reified C> textFromAsset(fileName: String): String? {
    val url: URL? = C::class.java.classLoader?.getResource(fileName)
    Log.d(TAG, "URL=${url?.path}")
    return url?.readText()
}

private const val TAG = "MOCK_WEB_SERVER"
