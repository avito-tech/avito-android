package com.avito.android.mock

import com.avito.test.http.RequestData
import okhttp3.mockwebserver.MockResponse

@Deprecated(
    "use same class from common module",
    replaceWith = ReplaceWith("com.avito.test.http.RequestCapturer")
)
class Mock(
    val requestMatcher: RequestData.() -> Boolean,
    val response: MockResponse,
    val removeAfterMatched: Boolean = false
)
