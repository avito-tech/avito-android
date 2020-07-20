package com.avito.test.http

import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer

class RequestData(val recordedRequest: RecordedRequest) {

    val path: String = recordedRequest.path ?: ""

    val method: String = recordedRequest.method ?: ""

    val body: String by lazy {
        val copyBody = Buffer()
        recordedRequest.body.copyTo(copyBody, 0, recordedRequest.bodySize)
        copyBody.readUtf8()
    }

    val headers: List<Pair<String, String>> by lazy {
        val headers = recordedRequest.headers
        headers.names().map { name -> name to headers.get(name)!! }
    }

    fun bodyContains(substring: CharSequence): Boolean {
        return this.body.contains(substring)
    }
}
