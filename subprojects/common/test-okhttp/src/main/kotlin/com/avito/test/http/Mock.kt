package com.avito.test.http

import okhttp3.mockwebserver.MockResponse

public class Mock(
    public val requestMatcher: RequestData.() -> Boolean,
    public val response: MockResponse,
    public val removeAfterMatched: Boolean = false
)
