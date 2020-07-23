package com.avito.test.http

import okhttp3.mockwebserver.RecordedRequest

class RequestData(val recordedRequest: RecordedRequest) {

    val path: String = recordedRequest.path ?: ""

    val method: String = recordedRequest.method ?: ""

    val body: String by lazy {
        recordedRequest.body
            .snapshot()
            .string(Charsets.UTF_8)
    }

    val headers: List<Pair<String, String>> by lazy {
        val headers = recordedRequest.headers
        headers.names().map { name -> name to headers[name]!! }
    }

    fun bodyContains(substring: CharSequence): Boolean {
        return this.body.contains(substring)
    }
}
