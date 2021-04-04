package com.avito.test.http

import okhttp3.mockwebserver.RecordedRequest

public class RequestData(
    public val recordedRequest: RecordedRequest
) {

    public val path: String = recordedRequest.path ?: ""

    public val method: String = recordedRequest.method ?: ""

    public val body: String by lazy {
        recordedRequest.body
            .snapshot()
            .string(Charsets.UTF_8)
    }

    public val headers: List<Pair<String, String>> by lazy {
        val headers = recordedRequest.headers
        headers.names().map { name -> name to headers[name]!! }
    }

    public fun bodyContains(substring: CharSequence): Boolean {
        return this.body.contains(substring)
    }
}
