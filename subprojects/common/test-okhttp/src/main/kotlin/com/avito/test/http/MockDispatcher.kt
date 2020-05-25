package com.avito.test.http

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.logging.Level
import java.util.logging.Logger

class MockDispatcher(
    private val defaultResponse: MockResponse = MockResponse().setResponseCode(418).setBody("Not mocked")
) : Dispatcher() {

    private val logger = Logger.getLogger(MockWebServer::class.java.name)

    init {
        logger.level = Level.WARNING
    }

    private val capturers = mutableListOf<RequestCapturer>()

    private val responses = mutableMapOf<RecordedRequest.() -> Boolean, MockResponse>()

    override fun dispatch(request: RecordedRequest): MockResponse {

        capturers.find { it.requestMatcher.invoke(request) }?.run {
            capture(request)
            logger.info("request captured: $request")
        }

        val response = responses
            .entries
            .find { (matcher, _) -> matcher(request) }
            ?.value ?: defaultResponse

        logger.info("got request: ${request.path}, response will be: $response")

        return response
    }

    fun mockResponse(
        requestMatcher: RecordedRequest.() -> Boolean,
        response: MockResponse
    ): MockDispatcher {
        responses[requestMatcher] = response
        return this
    }

    fun captureRequest(requestMatcher: RecordedRequest.() -> Boolean): RequestCapturer {
        val capturer = RequestCapturer(requestMatcher)
        capturers.add(capturer)
        return capturer
    }
}
