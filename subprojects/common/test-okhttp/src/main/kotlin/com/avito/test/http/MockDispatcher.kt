package com.avito.test.http

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MockDispatcher(
    private val defaultResponse: MockResponse = MockResponse().setResponseCode(418).setBody("Not mocked")
) : Dispatcher() {

    private val capturers = mutableListOf<RequestCapturer>()

    private val responses = mutableMapOf<RecordedRequest.() -> Boolean, MockResponse>()

    override fun dispatch(request: RecordedRequest): MockResponse {

        capturers.find { it.requestMatcher.invoke(request) }?.run {
            capture(request)
            println("request captured: $request")
        }

        val response = responses
            .entries
            .find { (matcher, _) -> matcher(request) }
            ?.value ?: defaultResponse

        println("got request: ${request.path}, response will be: $response")

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
