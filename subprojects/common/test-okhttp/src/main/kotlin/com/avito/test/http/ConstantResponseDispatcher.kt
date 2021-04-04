package com.avito.test.http

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

public open class ConstantResponseDispatcher(private val response: MockResponse) : Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse = response
}
