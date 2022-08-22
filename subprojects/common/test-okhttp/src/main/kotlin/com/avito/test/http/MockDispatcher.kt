package com.avito.test.http

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.PrintlnLoggerFactory
import com.avito.logger.create
import com.avito.utils.ResourcesReader
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.Collections

public class MockDispatcher(
    private val unmockedResponse: MockResponse = MockResponse().setResponseCode(418).setBody("Not mocked"),
    loggerFactory: LoggerFactory = PrintlnLoggerFactory
) : Dispatcher() {

    private val logger = loggerFactory.create<MockDispatcher>()

    private val mocks = Collections.synchronizedList(mutableListOf<Mock>())

    private val capturers = Collections.synchronizedList(mutableListOf<RequestCapturer>())

    public fun registerMock(mock: Mock) {
        mocks.add(mock)
    }

    public fun captureRequest(requestMatcher: RequestData.() -> Boolean): RequestCapturer {
        val capturer = RequestCapturer(requestMatcher)
        capturers.add(capturer)
        return capturer
    }

    public fun captureRequest(mock: Mock): RequestCapturer {
        val capturer = RequestCapturer(mock.requestMatcher)
        capturers.add(capturer)
        mocks.add(mock)
        return capturer
    }

    override fun dispatch(request: RecordedRequest): MockResponse {
        logger.info("Start dispatching request ${request.requestLine}")
        val requestData = RequestData(request)

        synchronized(capturers) {
            val foundCapturers = capturers.filter { it.requestMatcher.invoke(requestData) }
            if (foundCapturers.isNotEmpty()) {
                if (foundCapturers.size > 1) {
                    logger.warn("There are more then one capturer found for request ${request.requestLine}")
                }
                val foundCapturer = foundCapturers[0]
                foundCapturer.capture(request)
                logger.debug("Request ${request.requestLine} was captured")
            } else {
                logger.debug("Request ${request.requestLine} wasn't captured")
            }
        }

        /**
         * to be able to replace mocks (last one wins) (see MBS-5878)
         *
         * synchronized because of concurrent iterator access (see MBS-7636)
         */
        val lastMatchedMock = synchronized(mocks) {
            val matchedMocks = mocks.filter { it.requestMatcher.invoke(requestData) }
            if (matchedMocks.isNotEmpty()) {
                val mock = matchedMocks.last()
                if (mock.removeAfterMatched) {
                    mocks.remove(mock)
                }
                mock
            } else {
                null
            }
        }

        val response = if (lastMatchedMock != null) {
            logger.debug("Request matched: [$requestData], answering: ${lastMatchedMock.response}")
            lastMatchedMock.response
        } else {
            logger.warn("UnMocked request captured: [$requestData], answering: [$unmockedResponse]")
            unmockedResponse
        }

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
public fun MockResponse.setBodyFromFile(fileName: String): MockResponse {
    val text = ResourcesReader.readText(fileName)
    if (fileName.endsWith(".json")) {
        validateJson(text).onFailure {
            throw IllegalArgumentException("$fileName contains invalid json", it)
        }
    }
    setBody(text)
    return this
}

private fun validateJson(json: String): Result<Unit> = Result.tryCatch { gson.fromJson<JsonElement>(json) }
