package com.avito.android.mock

import okhttp3.mockwebserver.MockResponse

class Mock(
    val requestMatcher: RequestData.() -> Boolean,
    val response: MockResponse,
    val removeAfterMatched: Boolean = false
)
