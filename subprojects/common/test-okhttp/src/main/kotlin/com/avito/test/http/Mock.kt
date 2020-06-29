package com.avito.test.http

import okhttp3.mockwebserver.MockResponse

class Mock(
    val requestMatcher: RequestData.() -> Boolean,
    val response: MockResponse,
    val removeAfterMatched: Boolean = false
)
